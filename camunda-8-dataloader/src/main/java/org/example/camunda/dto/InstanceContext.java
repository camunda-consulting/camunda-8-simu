package org.example.camunda.dto;

public class InstanceContext {

  private Scenario scenario;
  private Double progress;

  public InstanceContext(Scenario scenario, Double progress) {
    this.scenario = scenario;
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
}
