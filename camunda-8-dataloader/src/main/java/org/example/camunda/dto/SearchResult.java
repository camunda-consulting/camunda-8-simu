package org.example.camunda.dto;

import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.search.TopDocs;

public class SearchResult<T> {

  private Integer total = 0;
  private Integer count = 0;
  private List<String> sortValues;
  private List<T> items;

  public SearchResult(List<T> list, Integer total, TopDocs topDocs) {
    this.count = list.size();
    this.total = total;
    if (list.size() > 0) {
      this.sortValues = new ArrayList<>();
      this.sortValues.add(topDocs.scoreDocs[0].doc + "_" + topDocs.scoreDocs[0].score);
      this.sortValues.add(
          topDocs.scoreDocs[count - 1].doc + "_" + topDocs.scoreDocs[count - 1].score);
    }
    this.items = list;
  }

  public Integer getTotal() {
    return total;
  }

  public void setTotal(Integer total) {
    this.total = total;
  }

  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public List<String> getSortValues() {
    return sortValues;
  }

  public void setSortValues(List<String> sortValues) {
    this.sortValues = sortValues;
  }

  public List<T> getItems() {
    return items;
  }

  public void setItems(List<T> items) {
    this.items = items;
  }

  public static <T> SearchResult<T> of(List<T> histo, Integer total, TopDocs topDocs) {
    return new SearchResult<T>(histo, total, topDocs);
  }
}
