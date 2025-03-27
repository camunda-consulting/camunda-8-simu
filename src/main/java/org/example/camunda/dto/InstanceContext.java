package org.example.camunda.dto;

public class InstanceContext {

  private Scenario scenario;
  private String processUniqueId;
  private Double progress;

  public InstanceContext(Scenario scenario, String processUniqueId, Double progress) {
    this.scenario = scenario;
    this.processUniqueId = processUniqueId;
    this.progress = progress;
  }

  public Scenario getScenario() {
    return scenario;
  }

  public void setScenario(Scenario scenario) {
    this.scenario = scenario;
  }

  public Double getProgress() {
    return progress;
  }

  public void setProgress(Double progress) {
    this.progress = progress;
  }

  public String getProcessUniqueId() {
    return processUniqueId;
  }

  public void setProcessUniqueId(String processUniqueId) {
    this.processUniqueId = processUniqueId;
  }
}
