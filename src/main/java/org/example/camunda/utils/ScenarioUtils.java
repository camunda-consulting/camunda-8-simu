package org.example.camunda.utils;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.camunda.feel.syntaxtree.ValDateTime;
import org.example.camunda.dto.InstanceContext;
import org.example.camunda.dto.InstanceStartTypeEnum;
import org.example.camunda.dto.ProgressionEnum;
import org.example.camunda.dto.Scenario;
import org.example.camunda.dto.StepDuration;
import org.example.camunda.dto.StepExecPlan;
import org.example.camunda.dto.progression.Evolution;
import org.example.camunda.dto.progression.ExponentialEvolution;
import org.example.camunda.dto.progression.LinearEvolution;
import org.example.camunda.dto.progression.NormalEvolution;
import org.example.camunda.dto.progression.SaltedLinearEvolution;
import org.example.camunda.dto.templating.JsonTemplate;
import org.w3c.dom.Document;

public class ScenarioUtils {

  public static Scenario generateScenario(String xml) {
    Document doc = BpmnUtils.getXmlDocument(xml);
    List<String> userTaskIds = BpmnUtils.getUserTasksElementsId(doc);
    List<String> serviceTaskIds = BpmnUtils.getServiceTasksElementsId(doc);

    Scenario result = new Scenario();
    result.setJsonTemplate(new JsonTemplate());
    result.setFirstDayFeelExpression("now() - duration(\"P3M\")");
    result.setLastDayFeelExpression("now()");
    LinearEvolution evol = new LinearEvolution();
    evol.setMax(100);
    evol.setMin(10);
    result.setEvolution(evol);
    result.setStartType(InstanceStartTypeEnum.START);
    result.setSteps(new HashMap<>());
    for (String eltId : userTaskIds) {
      StepExecPlan step = new StepExecPlan();
      step.setElementId(eltId);
      step.setDuration(new StepDuration());
      step.getDuration().setStartDesiredAvg(7200000);
      step.getDuration().setEndDesiredAvg(3600000);
      step.getDuration().setAvgProgression(ProgressionEnum.LINEAR_SALTED);
      step.setJsonTemplate(new JsonTemplate());
      result.getSteps().put(eltId, step);
    }
    for (String eltId : serviceTaskIds) {
      StepExecPlan step = new StepExecPlan();
      step.setElementId(eltId);
      step.setDuration(new StepDuration());
      step.setJsonTemplate(new JsonTemplate());
      result.getSteps().put(eltId, step);
    }
    return result;
  }

  public static long calculateInstancesPerDay(Evolution evol, double progress) {
    if (evol instanceof SaltedLinearEvolution) {
      return saltedLinearEvol((SaltedLinearEvolution) evol, progress);
    }
    if (evol instanceof ExponentialEvolution) {
      return exponentialEvol((ExponentialEvolution) evol, progress);
    }
    if (evol instanceof LinearEvolution) {
      return linearEvol((LinearEvolution) evol, progress);
    }
    if (evol instanceof NormalEvolution) {
      return normalEvol((NormalEvolution) evol, progress);
    }

    return 0;
  }

  private static long normalEvol(NormalEvolution evol, double progress) {
    NormalDistribution distrib = getNormalDistribution(evol.getMean(), evol.getDerivation());
    double distribMin = evol.getMean() > 0.5 ? distrib.density(0) : distrib.density(1);
    double distribMax = distrib.density(evol.getMean());
    double multiplier = (evol.getMax() - evol.getMin()) / (distribMax - distribMin);
    return Math.round(evol.getMin() + (distrib.density(progress) - distribMin) * multiplier);
  }

  private static long exponentialEvol(ExponentialEvolution evol, double progress) {
    double x = progress;
    if (evol.isDecreasing()) {
      x = 1 - x;
    }
    final int result =
        evol.getMin() + (int) (Math.pow(x, evol.getExponent()) * (evol.getMax() - evol.getMin()));
    return result;
  }

  private static long linearEvol(LinearEvolution evol, double progress) {
    double x = progress;
    if (evol.isDecreasing()) {
      x = 1 - x;
    }
    int difference = evol.getMax() - evol.getMin();

    return evol.getMin() + Math.round(difference * x);
  }

  private static long saltedLinearEvol(SaltedLinearEvolution evol, double progress) {
    double x = progress;
    if (evol.isDecreasing()) {
      x = 1 - x;
    }
    int difference = evol.getMax() - evol.getMin();
    long saltDif = evol.getSaltMax() - evol.getSaltMin();
    long salt = Math.round(Math.random() * saltDif) + evol.getSaltMin();
    long multiplier = Math.random() < 0.5 ? -1 : 1;
    return evol.getMin() + Math.round(difference * x) + salt * multiplier;
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
    if (ContextUtils.getInstanceDistribution() == ChronoUnit.DAYS
        || ContextUtils.getInstanceDistribution() == ChronoUnit.HALF_DAYS
        || ContextUtils.getInstanceDistribution() == ChronoUnit.HOURS) {
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

  public static Long getEstimatedTime(long date, String feelDuration) {
    return getMillis(
        FeelUtils.evaluate(
            "date and time(\""
                + Instant.ofEpochMilli(date).toString()
                + "\") + duration(\""
                + feelDuration
                + "\")",
            new HashMap<>(),
            ValDateTime.class));
  }

  public static Long getMillis(ValDateTime valDate) {
    return Instant.parse(valDate.toString()).toEpochMilli();
  }

  public static ZonedDateTime getZonedDateDay(String feelExpression) {
    return FeelUtils.evaluate(feelExpression, ValDateTime.class).value();
  }

  private static Map<String, NormalDistribution> normalDistributionRegistry = new HashMap<>();

  private static NormalDistribution getNormalDistribution(double mean, double standardDeviation) {
    String name = mean + "_" + standardDeviation;
    NormalDistribution normalDistribution = normalDistributionRegistry.get(name);
    if (normalDistribution == null) {
      normalDistribution = new NormalDistribution(mean, standardDeviation);
      normalDistributionRegistry.put(name, normalDistribution);
    }
    return normalDistribution;
  }
}
