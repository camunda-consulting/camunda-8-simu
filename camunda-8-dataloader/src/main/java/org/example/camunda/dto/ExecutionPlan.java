package org.example.camunda.dto;

import io.camunda.operate.model.ProcessDefinition;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ExecutionPlan {
  private ProcessDefinition definition;
  private String xml;
  private Boolean xmlModified = false;
  private ChronoUnit instanceDistribution = ChronoUnit.DAYS;
  private TimePrecisionEnum timePrecision = TimePrecisionEnum.HUNDRED_MILLIS;
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

  public ChronoUnit getInstanceDistribution() {
    return instanceDistribution;
  }

  public void setInstanceDistribution(ChronoUnit instanceDistribution) {
    this.instanceDistribution = instanceDistribution;
  }

  public TimePrecisionEnum getTimePrecision() {
    return timePrecision;
  }

  public void setTimePrecision(TimePrecisionEnum timePrecision) {
    this.timePrecision = timePrecision;
  }

  public List<Scenario> getScenarii() {
    return scenarii;
  }

  public void setScenarii(List<Scenario> scenarii) {
    this.scenarii = scenarii;
  }
}
