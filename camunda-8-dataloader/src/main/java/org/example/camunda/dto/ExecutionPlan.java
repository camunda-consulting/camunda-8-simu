package org.example.camunda.dto;

import io.camunda.operate.model.ProcessDefinition;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ExecutionPlan {
  private ProcessDefinition definition;
  private String xml;
  private Boolean xmlModified = false;
  private ChronoUnit precision = ChronoUnit.DAYS;
  private List<Scenario> scenarii = new ArrayList<>();

  public ProcessDefinition getDefinition() {
    return definition;
  }

  public void setDefinition(ProcessDefinition definition) {
    this.definition = definition;
  }

  public String getXml() {
    return xml;
  }

  public void setXml(String xml) {
    this.xml = xml;
  }

  public Boolean getXmlModified() {
    return xmlModified;
  }

  public void setXmlModified(Boolean xmlModified) {
    this.xmlModified = xmlModified;
  }

  public ChronoUnit getPrecision() {
    return precision;
  }

  public void setPrecision(ChronoUnit precision) {
    this.precision = precision;
  }

  public List<Scenario> getScenarii() {
    return scenarii;
  }

  public void setScenarii(List<Scenario> scenarii) {
    this.scenarii = scenarii;
  }
}
