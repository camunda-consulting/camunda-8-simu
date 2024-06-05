package org.example.camunda.utils;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import org.camunda.feel.syntaxtree.ValDateTime;
import org.example.camunda.dto.InstanceContext;
import org.example.camunda.dto.InstanceStartTypeEnum;
import org.example.camunda.dto.ProgressionEnum;
import org.example.camunda.dto.Scenario;
import org.example.camunda.dto.StepDuration;
import org.example.camunda.dto.StepExecPlan;
import org.w3c.dom.Document;

public class ScenarioUtils {

  public static Scenario generateScenario(String xml) {
    Document doc = BpmnUtils.getXmlDocument(xml);
    List<String> userTaskIds = BpmnUtils.getUserTasksElementsId(doc);
    List<String> serviceTaskIds = BpmnUtils.getServiceTasksElementsId(doc);

    Scenario result = new Scenario();
    result.setFirstDayFeelExpression("now() - duration(\"P3M\")");
    result.setLastDayFeelExpression("now()");
    result.setNbInstancesStart(10);
    result.setNbInstancesEnd(100);
    result.setEvolution(ProgressionEnum.LINEAR);
    result.setStartType(InstanceStartTypeEnum.START);
    result.setSteps(new HashMap<>());
    for (String eltId : userTaskIds) {
      StepExecPlan step = new StepExecPlan();
      step.setElementId(eltId);
      step.setDuration(new StepDuration());
      step.getDuration().setStartDesiredAvg(7200000);
      step.getDuration().setEndDesiredAvg(3600000);
      step.getDuration().setAvgProgression(ProgressionEnum.LINEAR_SALTED);

      result.getSteps().put(eltId, step);
    }
    for (String eltId : serviceTaskIds) {
      StepExecPlan step = new StepExecPlan();
      step.setElementId(eltId);
      step.setDuration(new StepDuration());
      result.getSteps().put(eltId, step);
    }
    return result;
  }

  public static long calculateInstancesPerDay(Scenario scenario, double progress) {

    int difference = scenario.getNbInstancesEnd() - scenario.getNbInstancesStart();
    if (scenario.getEvolution() == ProgressionEnum.LINEAR) {
      return scenario.getNbInstancesStart() + Math.round(difference * progress);
    }
    if (scenario.getEvolution() == ProgressionEnum.LINEAR_SALTED) {
      long saltDif = scenario.getSaltMax() - scenario.getSaltMin();
      long salt = Math.round(Math.random() * saltDif) + scenario.getSaltMin();
      long multiplier = Math.random() < 0.5 ? -1 : 1;
      return scenario.getNbInstancesStart() + Math.round(difference * progress) + salt * multiplier;
    }
    return 0;
  }

  public static long calculateTaskDuration(StepExecPlan step, String processUniqueId) {
    InstanceContext context = ContextUtils.getContext(processUniqueId);
    StepDuration duration = step.getDuration();
    long desiredAvg =
        duration.getStartDesiredAvg()
            + Math.round(
                (duration.getEndDesiredAvg() - duration.getStartDesiredAvg())
                    * context.getProgress());
    if (duration.getAvgProgression() == ProgressionEnum.LINEAR_SALTED) {
      long multiplier = Math.random() < 0.5 ? -1 : 1;
      long salt = Math.round(Math.random() * duration.getProgressionSalt() * multiplier);
      desiredAvg = desiredAvg + salt;
    }
    if (context.getScenario().getInstanceDistribution() == ChronoUnit.DAYS
        || context.getScenario().getInstanceDistribution() == ChronoUnit.HALF_DAYS
        || context.getScenario().getInstanceDistribution() == ChronoUnit.HOURS) {
      if (ContextUtils.shouldComputeMin(processUniqueId)) {
        return desiredAvg - duration.getMinMaxPercent() * desiredAvg / 100;
      }
      if (ContextUtils.shouldComputeMax(processUniqueId)) {
        return desiredAvg + duration.getMinMaxPercent() * desiredAvg / 100;
      }

      return desiredAvg;
    }
    // in case precision is minutes, we will salt the duration between the min/max
    long salt =
        Math.round(
            (Math.random() * duration.getMinMaxPercent() * 2 - duration.getMinMaxPercent())
                * desiredAvg);
    return desiredAvg + salt;
  }

  public static Long getTimerCatchEventTime(String flowNodeId, long creationTime) {
    if (ContextUtils.isDateTimer(flowNodeId)) {
      String date = ContextUtils.getDateTimer(flowNodeId);
      if (date.startsWith("=")) {
        return getMillis(FeelUtils.evaluate(date.substring(1), new HashMap<>(), ValDateTime.class));
      }
      return Instant.parse(date.substring(1)).toEpochMilli();
    } else if (ContextUtils.isDurationTimer(flowNodeId)) {
      String duration = ContextUtils.getDurationTimer(flowNodeId);
      String creationDate = Instant.ofEpochMilli(creationTime).toString();
      return getMillis(
          FeelUtils.evaluate(
              "date and time(\"" + creationDate + "\") + duration(\"" + duration + "\")",
              new HashMap<>(),
              ValDateTime.class));
    }
    return null;
  }

  public static Long getMillis(ValDateTime valDate) {
    return Instant.parse(valDate.toString()).toEpochMilli();
  }

  public static ZonedDateTime getZonedDateDay(String feelExpression) {
    return FeelUtils.evaluate(feelExpression, ValDateTime.class).value();
  }
}
