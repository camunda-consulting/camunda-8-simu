package org.example.camunda.dto.templating;

import java.util.List;
import java.util.Map;

public class Dataset {
  String name;
  Map<String, List<Object>> categorizedData;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, List<Object>> getCategorizedData() {
    return categorizedData;
  }

  public void setCategorizedData(Map<String, List<Object>> categorizedData) {
    this.categorizedData = categorizedData;
  }
}
