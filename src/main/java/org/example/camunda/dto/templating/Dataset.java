package org.example.camunda.dto.templating;

import java.util.List;
import java.util.Map;

public class Dataset {
  String name;
  Map<String, List<String>> categorizedData;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, List<String>> getCategorizedData() {
    return categorizedData;
  }

  public void setCategorizedData(Map<String, List<String>> categorizedData) {
    this.categorizedData = categorizedData;
  }
}
