package org.example.camunda.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import org.example.camunda.dto.templating.Dataset;
import org.example.camunda.dto.templating.JsonDataset;
import org.example.camunda.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DatasetUtils {
  private static final Logger LOG = LoggerFactory.getLogger(DatasetUtils.class);
  private static final String DATASETS_REPO = "datasets";
  private static final String JSONDATASETS_REPO = "jsondatasets";

  private static String workspace = "workspace";

  private static final Map<String, Dataset> datasets = new HashMap<>();
  private static final Map<String, JsonDataset> jsonDatasets = new HashMap<>();

  public static Path resolveDataset(String datasetName) {
    return Path.of(workspace).resolve(DATASETS_REPO).resolve(datasetName);
  }

  public static Path resolveJsonDataset(String datasetName) {
    return Path.of(workspace).resolve(JSONDATASETS_REPO).resolve(datasetName);
  }

  public static List<String> listDatasets() {
    return Stream.of(Path.of(workspace).resolve(DATASETS_REPO).toFile().listFiles())
            .map(File::getName)
            .collect(Collectors.toList());
  }
  public static List<String> listJsonDatasets() {
    return Stream.of(Path.of(workspace).resolve(JSONDATASETS_REPO).toFile().listFiles())
            .map(File::getName)
            .collect(Collectors.toList());
  }

  public static Dataset getDataset(String name) throws IOException {
    if (datasets.containsKey(name)) {
      return datasets.get(name);
    }
    Path path = resolveDataset(name);
    if (!path.toFile().exists()) {
      return null;
    }
    datasets.put(name, JsonUtils.fromJsonFile(path, Dataset.class));
    return datasets.get(name);
  }
  public static JsonDataset getJsonDataset(String name) throws IOException {
    if (jsonDatasets.containsKey(name)) {
      return jsonDatasets.get(name);
    }
    Path path = resolveJsonDataset(name);
    if (!path.toFile().exists()) {
      return null;
    }
    jsonDatasets.put(name, JsonUtils.fromJsonFile(path, JsonDataset.class));
    return jsonDatasets.get(name);
  }

  public static void init() throws IOException {
    // populate datasets
    List<String>  list = listDatasets();
    for (String d : list) {
      getDataset(d);
    }
     list = listJsonDatasets();
    for (String d : list) {
      getJsonDataset(d);
    }
  }

  public static void saveDataset(Dataset dataset) throws IOException {
    JsonUtils.toJsonFile(resolveDataset(dataset.getName()), dataset);
    datasets.put(dataset.getName(), dataset);
  }

  public static void saveJsonDataset(JsonDataset dataset) throws IOException {
    JsonUtils.toJsonFile(resolveJsonDataset(dataset.getName()), dataset);
    jsonDatasets.put(dataset.getName(), dataset);
  }

  public static void deleteDataset(String name) throws IOException {    Files.delete(resolveJsonDataset(name));
    Files.delete(resolveDataset(name));
    datasets.remove(name);
  }

  public static void deleteJsonDataset(String name) throws IOException {
    Files.delete(resolveJsonDataset(name));
    jsonDatasets.remove(name);
  }
}
