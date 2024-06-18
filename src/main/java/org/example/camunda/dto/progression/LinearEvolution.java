package org.example.camunda.dto.progression;

public class LinearEvolution extends Evolution {

  public boolean decreasing;

  public boolean isDecreasing() {
    return decreasing;
  }

  public void setDecreasing(boolean decreasing) {
    this.decreasing = decreasing;
  }
}
