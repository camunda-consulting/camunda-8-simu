package org.example.camunda.utils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.camunda.dto.ExecutionPlan;

public class HistoUtils {

  private static String planCode = null;
  private static Map<String, ExecutionPlan> plans = new HashMap<>();
  private static Map<String, List<String>> planHisto = new HashMap<>();
  private static Map<String, Integer> planExecuted = new HashMap<>();
  private static Map<String, Integer> planRemaining = new HashMap<>();

  public static void addHisto(String comment) {
    getHisto().add(Instant.now().toString() + " : " + comment);
  }

  public static List<String> getHisto() {
    if (planCode == null) {
      ExecutionPlan plan = ContextUtils.getPlan();
      planCode = plan.getDefinition().getBpmnProcessId() + "_" + Instant.now().toString();
      plans.put(planCode, plan);
    }
    return getHisto(planCode);
  }

  public static List<String> getHisto(String plan) {
    if (!planHisto.containsKey(plan)) {
      planHisto.put(plan, new ArrayList<>());
    }
    return planHisto.get(plan);
  }

  public static void endPlan() {
    planCode = null;
  }

  public static void clean() {
    planExecuted.clear();
    planRemaining.clear();
    planHisto.clear();
    plans.clear();
    planCode = null;
  }

  public static synchronized void updateProgress(int nbEntries) {
    if (planExecuted.containsKey(planCode)) {
      planExecuted.put(planCode, planExecuted.get(planCode) + 1);
    } else {
      planExecuted.put(planCode, 1);
    }
    planRemaining.put(planCode, nbEntries);
  }

  public static int getProgress() {
    int executed = planExecuted.get(planCode);
    int remaining = planRemaining.get(planCode);
    return (executed * 100) / (executed + remaining);
  }

  public static Map<String, Integer> getProgresses() {
    Map<String, Integer> result = new HashMap<>();
    for (String plan : plans.keySet()) {
      int executed = planExecuted.get(planCode);
      int remaining = planRemaining.get(planCode);
      result.put(plan, (executed * 100) / (executed + remaining));
    }
    return result;
  }

  public static Map<String, ExecutionPlan> getPlans() {
    return plans;
  }
}
