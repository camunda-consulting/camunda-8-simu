package org.example.camunda.dto;

import io.camunda.operate.model.ProcessDefinition;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExecutionPlan {
  private ProcessDefinition definition;
  private String xml;
  private Map<String, String> xmlDependencies;
  private Boolean xmlModified = false;
  private Integer idleTimeBeforeClockMove = 300;
  private ChronoUnit instanceDistribution = ChronoUnit.DAYS;
  private TimePrecisionEnum timePrecision = TimePrecisionEnum.HUNDRED_MILLIS;
  private String durationsType = "FEEL";
  private List<Scenario> scenarii = new ArrayList<>();
  private String stepLabel = "id";

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

  public Map<String, String> getXmlDependencies() {
    return xmlDependencies;
  }

  public void setXmlDependencies(Map<String, String> xmlDependencies) {
    this.xmlDependencies = xmlDependencies;
  }

  public Boolean getXmlModified() {
    return xmlModified;
  }

  public void setXmlModified(Boolean xmlModified) {
    this.xmlModified = xmlModified;
  }

  public Integer getIdleTimeBeforeClockMove() {
    return idleTimeBeforeClockMove;
  }

  public void setIdleTimeBeforeClockMove(Integer idleTimeBeforeClockMove) {
    this.idleTimeBeforeClockMove = idleTimeBeforeClockMove;
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

  public String getDurationsType() {
    return durationsType;
  }

  public void setDurationsType(String durationsType) {
    this.durationsType = durationsType;
  }

  public List<Scenario> getScenarii() {
    return scenarii;
  }

  public void setScenarii(List<Scenario> scenarii) {
    this.scenarii = scenarii;
  }

  public String getStepLabel() {
    return stepLabel;
  }

  public void setStepLabel(String stepLabel) {
    this.stepLabel = stepLabel;
  }
}
