package org.example.camunda.utils;

import io.camunda.zeebe.client.api.worker.JobWorker;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.example.camunda.core.actions.Action;
import org.example.camunda.dto.ExecutionPlan;
import org.example.camunda.dto.InstanceContext;
import org.example.camunda.dto.Scenario;

public class ContextUtils {

  private static long estimateEngineTime;
  private static ExecutionPlan currentPlan = null;
  private static Map<String, String> dateTimers = new HashMap<>();
  private static Map<String, String> durationTimers = new HashMap<>();
  private static SortedMap<Long, List<Action>> timedActions = new TreeMap<>();
  private static Map<String, InstanceContext> processInstanceScenarioMap = new HashMap<>();
  private static Set<String> minHandledMap = new HashSet<>();
  private static Set<String> maxHandledMap = new HashSet<>();
  private static Set<String> minProcessInstances = new HashSet<>();
  private static Set<String> maxProcessInstances = new HashSet<>();
  private static List<JobWorker> activeWorkers = new ArrayList<>();

  public static void addInstance(String uniqueId, Scenario scenario, Double progress) {
    processInstanceScenarioMap.put(uniqueId, new InstanceContext(scenario, progress));
    prepareMinMax(uniqueId, scenario, progress);
  }

  private static synchronized void prepareMinMax(
      String processUniqueId, Scenario scenario, Double progress) {
    if (!minHandledMap.contains(scenario.getName() + progress)) {
      minHandledMap.add(scenario.getName() + progress);
      minProcessInstances.add(processUniqueId);
    } else if (!maxHandledMap.contains(scenario.getName() + progress)) {
      maxHandledMap.add(scenario.getName() + progress);
      maxProcessInstances.add(processUniqueId);
    }
  }

  public static Double getProgress(String processUniqueId) {
    return getContext(processUniqueId).getProgress();
  }

  public static InstanceContext getContext(String processUniqueId) {
    return processInstanceScenarioMap.get(processUniqueId);
  }

  public static void addWorker(JobWorker worker) {
    activeWorkers.add(worker);
  }

  public static long addAction(long time, Action action) {
    if (time < System.currentTimeMillis()) {
      long realTime = buildEntry(ContextUtils.getPlan().getTimePrecision().round(time));
      timedActions.get(realTime).add(action);
      return realTime;
    } else {
      return -1;
    }
  }

  public static long buildEntry(long time) {
    if (time < System.currentTimeMillis() && !timedActions.containsKey(time)) {
      timedActions.put(time, new ArrayList<>());
    }
    return time;
  }

  public static long nextTimeEntry() {
    return timedActions.firstKey();
  }

  public static List<Action> getActionsAt(long time) {
    return timedActions.get(time);
  }

  public static void removeTimeEntry(long time) {
    timedActions.remove(time);
  }

  public static boolean hasTimeEntries() {
    return !timedActions.isEmpty();
  }

  public static int nbEntries() {
    return timedActions.size();
  }

  public static void endPlan() {
    HistoUtils.endPlan();
    currentPlan = null;
    timedActions.clear();
    durationTimers.clear();
    dateTimers.clear();
    processInstanceScenarioMap.clear();
    minHandledMap.clear();
    maxHandledMap.clear();
    minProcessInstances.clear();
    maxProcessInstances.clear();
    for (JobWorker worker : activeWorkers) {
      worker.close();
    }
    activeWorkers.clear();
    estimateEngineTime = 0;
  }

  public static boolean shouldComputeMin(String processUniqueId) {
    return minProcessInstances.contains(processUniqueId);
  }

  public static boolean shouldComputeMax(String processUniqueId) {
    return maxProcessInstances.contains(processUniqueId);
  }

  public static void addDateTimer(String flowNodeId, String date) {
    dateTimers.put(flowNodeId, date);
  }

  public static void addDurationTimer(String flowNodeId, String durationFeelExpression) {
    durationTimers.put(flowNodeId, durationFeelExpression);
  }

  public static boolean isDateTimer(String flowNodeId) {
    return dateTimers.containsKey(flowNodeId);
  }

  public static boolean isDurationTimer(String flowNodeId) {
    return durationTimers.containsKey(flowNodeId);
  }

  public static String getDateTimer(String flowNodeId) {
    return dateTimers.get(flowNodeId);
  }

  public static String getDurationTimer(String flowNodeId) {
    return durationTimers.get(flowNodeId);
  }

  public static int getIdleTimeBeforeClockMove() {
    return currentPlan.getIdleTimeBeforeClockMove();
  }

  public static void setPlan(ExecutionPlan plan) {
    currentPlan = plan;
  }

  public static ExecutionPlan getPlan() {
    return currentPlan;
  }

  public static long getEngineTime() {
    return estimateEngineTime;
  }

  public static void setEngineTime(long estimateEngineTime) {
    ContextUtils.estimateEngineTime = estimateEngineTime;
  }

  public static ChronoUnit getInstanceDistribution() {
    return currentPlan.getInstanceDistribution();
  }
}
