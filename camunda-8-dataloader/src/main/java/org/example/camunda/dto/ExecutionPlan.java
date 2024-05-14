package org.example.camunda.dto;

import io.camunda.operate.model.ProcessDefinition;
import java.util.ArrayList;
import java.util.List;

public class ExecutionPlan {
  private ProcessDefinition definition;
  private String xml;
  private Boolean xmlModified = false;
  private PlanPrecisionEnum precision = PlanPrecisionEnum.DAY;
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

  public PlanPrecisionEnum getPrecision() {
    return precision;
  }

  public void setPrecision(PlanPrecisionEnum precision) {
    this.precision = precision;
  }

  public List<Scenario> getScenarii() {
    return scenarii;
  }

  public void setScenarii(List<Scenario> scenarii) {
    this.scenarii = scenarii;
  }
}
