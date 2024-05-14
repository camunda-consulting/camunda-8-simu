package org.example.camunda.utils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import org.example.camunda.dto.Scenario;
import org.example.camunda.dto.ScenarioProgressionEnum;
import org.example.camunda.dto.StepExecPlan;
import org.w3c.dom.Document;

public class ScenarioUtils {

  public static Scenario generateScenario(String xml) {
    Document doc = BpmnUtils.getXmlDocument(xml);
    List<String> userTaskIds = BpmnUtils.getUserTasksElementsId(doc);
    List<String> serviceTaskIds = BpmnUtils.getServiceTasksElementsId(doc);

    Scenario result = new Scenario();
    result.setFirstDay(LocalDate.now().minusMonths(3));
    result.setLastDay(LocalDate.now());
    result.setNbInstancesStart(10);
    result.setNbInstancesEnd(100);
    result.setEvolution(ScenarioProgressionEnum.LINEAR);
    result.setSteps(new HashMap<>());
    for (String eltId : userTaskIds) {
      StepExecPlan step = new StepExecPlan();
      step.setElementId(eltId);
      step.setAvgDuration(3600000);
      step.setMaxDuration(14400000);
      step.setMinDuration(300000);

      result.getSteps().put(eltId, step);
    }
    for (String eltId : serviceTaskIds) {
      StepExecPlan step = new StepExecPlan();
      step.setElementId(eltId);
      result.getSteps().put(eltId, step);
    }
    return result;
  }
}
