package org.example.camunda.dto.progression;

public class NormalEvolution extends Evolution {

  private double mean;
  private double derivation;

  public double getMean() {
    return mean;
  }

  public void setMean(double mean) {
    this.mean = mean;
  }

  public double getDerivation() {
    return derivation;
  }

  public void setDerivation(double derivation) {
    this.derivation = derivation;
  }
}
