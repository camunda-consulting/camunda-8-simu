package org.example.camunda.dto.templating;

import java.util.List;
import java.util.Map;

public class Dataset {
  String name;
  List<Object> data;
  Map<String, List<Object>> localizedData;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Object> getData() {
    return data;
  }

  public void setData(List<Object> data) {
    this.data = data;
  }

  public Map<String, List<Object>> getLocalizedData() {
    return localizedData;
  }

  public void setLocalizedData(Map<String, List<Object>> localizedData) {
    this.localizedData = localizedData;
  }
}
