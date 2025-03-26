package org.example.camunda.dto;

public class InstancesToStart {
  private final Long time;
  private final Scenario scenario;
  private final double progress;
  private final long nbInstances;

  public InstancesToStart(long time, Scenario scenario, double progress, long nbInstances) {
    this.time = time;
    this.scenario = scenario;
    this.progress = progress;
    this.nbInstances = nbInstances;
  }

  public long getTime() {
    return time;
  }

  public Scenario getScenario() {
    return scenario;
  }

  public double getProgress() {
    return progress;
  }

  public long getNbInstances() {
    return nbInstances;
  }
}
