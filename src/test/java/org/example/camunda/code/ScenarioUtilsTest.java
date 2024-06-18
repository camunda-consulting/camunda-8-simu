package org.example.camunda.code;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.example.camunda.dto.progression.NormalEvolution;
import org.example.camunda.utils.ScenarioUtils;
import org.junit.jupiter.api.Test;

public class ScenarioUtilsTest {
  @Test
  public void normalEvol() {
    NormalEvolution evol = new NormalEvolution();
    evol.setMin(10);
    evol.setMax(100);
    evol.setMean(0.4);
    evol.setDerivation(0.2);
    long nb = ScenarioUtils.calculateInstancesPerDay(evol, 0.4);
    assertEquals(100, nb);
    nb = ScenarioUtils.calculateInstancesPerDay(evol, 1);
    assertEquals(10, nb);
  }
}
