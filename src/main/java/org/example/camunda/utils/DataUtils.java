package org.example.camunda.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.example.camunda.dto.templating.Dataset;

public class DataUtils {

  private static Map<String, Dataset> datasets;

  private static Path getDatasetPath() {
    return Path.of("workspace").resolve("datasets.json");
  }

  public static Map<String, Dataset> getDatasets() {
    if (datasets == null) {
      Path path = getDatasetPath();
      if (!path.toFile().exists()) {
        datasets = new HashMap<>();
      }
      try {
        datasets = JsonUtils.fromJsonFile(path, new TypeReference<Map<String, Dataset>>() {});
      } catch (IOException e) {
        datasets = new HashMap<>();
      }
    }
    return datasets;
  }

  public static Dataset getDataset(String name) {
    return getDatasets().get(name);
  }

  public static void saveDataset(Dataset dataset) throws IOException {
    getDatasets().put(dataset.getName(), dataset);
    JsonUtils.toJsonFile(getDatasetPath(), datasets);
  }

  public static void deleteDataset(String name) throws IOException {
    getDatasets().remove(name);
    JsonUtils.toJsonFile(getDatasetPath(), datasets);
  }
}
