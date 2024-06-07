package org.example.camunda.dto.templating;

import java.util.HashMap;
import java.util.Map;

public class JsonTemplate {

  private String template;
  private Map<String, Object> exampleContext;

  public JsonTemplate() {
    template = "{}";
    exampleContext = new HashMap<>();
  }

  public String getTemplate() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  public Map<String, Object> getExampleContext() {
    return exampleContext;
  }

  public void setExampleContext(Map<String, Object> exampleContext) {
    this.exampleContext = exampleContext;
  }
}
