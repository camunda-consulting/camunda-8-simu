package org.example.camunda.dto.templating;

import java.util.List;

public class JsonDataset {
  String name;
  List<String> data;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getData() {
    return data;
  }

  public void setData(List<String> data) {
    this.data = data;
  }
}
