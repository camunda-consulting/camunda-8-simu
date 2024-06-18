package org.example.camunda.dto.progression;

public class ExponentialEvolution extends LinearEvolution {
  public int exponent;

  public int getExponent() {
    return exponent;
  }

  public void setExponent(int exponent) {
    this.exponent = exponent;
  }
}
