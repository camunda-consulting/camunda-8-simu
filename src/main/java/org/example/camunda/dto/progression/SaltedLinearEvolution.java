package org.example.camunda.dto.progression;

public class SaltedLinearEvolution extends LinearEvolution {
  private Integer saltMin;
  private Integer saltMax;

  public Integer getSaltMin() {
    return saltMin;
  }

  public void setSaltMin(Integer saltMin) {
    this.saltMin = saltMin;
  }

  public Integer getSaltMax() {
    return saltMax;
  }

  public void setSaltMax(Integer saltMax) {
    this.saltMax = saltMax;
  }
}
