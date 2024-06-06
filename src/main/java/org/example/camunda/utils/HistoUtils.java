package org.example.camunda.utils;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.example.camunda.dto.ExecutionPlan;
import org.example.camunda.dto.HistoLog;
import org.example.camunda.dto.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistoUtils {
  private static final Logger LOG = LoggerFactory.getLogger(HistoUtils.class);

  private static String planCode = null;
  private static Map<String, ExecutionPlan> plans = new HashMap<>();
  private static Map<String, Integer> planExecuted = new HashMap<>();
  private static Map<String, Integer> planRemaining = new HashMap<>();

  private static Directory memoryIndex = new ByteBuffersDirectory();

  private static IndexWriter getLuceneWriter() throws IOException {
    StandardAnalyzer analyzer = new StandardAnalyzer();
    IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
    return new IndexWriter(memoryIndex, indexWriterConfig);
  }

  private static IndexReader getLuceneReader() throws IOException {
    return DirectoryReader.open(memoryIndex);
  }

  private static HistoLog buildHistoLog(String comment) {
    HistoLog log = new HistoLog();
    log.setBpmnProcessId(ContextUtils.getPlan().getDefinition().getBpmnProcessId());
    log.setPlanCode(getPlanCode());
    log.setLog(comment);
    log.setRealDate(Instant.now().toEpochMilli());
    log.setEngineDate(ContextUtils.getEngineTime());
    return log;
  }

  private static Document getDocument(HistoLog log) throws IOException {
    Document document = new Document();

    document.add(new TextField("bpmnProcessId", log.getBpmnProcessId(), Field.Store.NO));
    document.add(new TextField("planCode", log.getPlanCode(), Field.Store.NO));
    document.add(new TextField("log", JsonUtils.toJson(log), Field.Store.YES));
    document.add(new TextField("realDate", String.valueOf(log.getRealDate()), Field.Store.NO));
    document.add(new TextField("engineDate", String.valueOf(log.getEngineDate()), Field.Store.NO));
    return document;
  }

  public static void addHisto(String comment) {
    HistoLog log = buildHistoLog(comment);
    // getHisto().add(Instant.now().toString() + " : " + comment);
    try {
      Document document = getDocument(log);
      IndexWriter writer = getLuceneWriter();
      writer.addDocument(document);
      writer.commit();
      writer.close();
    } catch (IOException e) {
      LOG.error("error indexing doc in lucence", e);
    }
  }

  public static String getPlanCode() {
    if (planCode == null) {
      ExecutionPlan plan = ContextUtils.getPlan();
      if (plan != null) {
        planCode = plan.getDefinition().getBpmnProcessId() + "_" + Instant.now().toString();
        plans.put(planCode, plan);
      }
    }
    return planCode;
  }

  public static SearchResult<HistoLog> search(
      String query, String before, String after, Integer pageSize) {
    TopDocs topDocs = null;
    Integer total = null;
    List<HistoLog> result = new ArrayList<>();
    if (pageSize == null) {
      pageSize = 10;
    }
    try {
      QueryParser parser = new QueryParser("log", new StandardAnalyzer());
      parser.setDefaultOperator(QueryParser.Operator.AND);
      IndexReader reader = getLuceneReader();
      IndexSearcher searcher = new IndexSearcher(reader);
      Query luceneQuery = parser.parse(query);
      total = searcher.count(luceneQuery);
      if (after != null) {
        int indexSep = after.indexOf("_");
        Integer doc = Integer.valueOf(after.substring(0, indexSep));
        Float score = Float.valueOf(after.substring(indexSep + 1));
        topDocs = searcher.searchAfter(new ScoreDoc(doc, score), luceneQuery, pageSize);
      } else {
        topDocs = searcher.search(luceneQuery, pageSize);
      }

      for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
        Document doc = reader.storedFields().document(scoreDoc.doc);
        result.add(JsonUtils.toObject(doc.get("log"), HistoLog.class));
      }
      reader.close();
    } catch (IOException | ParseException e) {
      LOG.error("Error searching lucene", e);
    }
    return SearchResult.of(result, total, topDocs);
  }

  public static void endPlan() {
    updateProgress(0);
    planCode = null;
  }

  public static void clean() {
    planExecuted.clear();
    planRemaining.clear();
    plans.clear();
    planCode = null;
  }

  public static synchronized void updateProgress(int nbEntries) {
    if (planExecuted.containsKey(planCode)) {
      planExecuted.put(planCode, planExecuted.get(planCode) + 1);
    } else {
      planExecuted.put(planCode, 1);
    }
    planRemaining.put(planCode, nbEntries);
  }

  public static int getProgress() {
    int executed = planExecuted.get(planCode);
    int remaining = planRemaining.get(planCode);
    return (executed * 100) / (executed + remaining);
  }

  public static Map<String, Integer> getProgresses() {
    Map<String, Integer> result = new HashMap<>();
    for (String plan : plans.keySet()) {
      int executed = planExecuted.get(plan);
      int remaining = planRemaining.get(plan);
      result.put(plan, (executed * 100) / (executed + remaining));
    }
    return result;
  }

  public static Map<String, ExecutionPlan> getPlans() {
    return plans;
  }
}
