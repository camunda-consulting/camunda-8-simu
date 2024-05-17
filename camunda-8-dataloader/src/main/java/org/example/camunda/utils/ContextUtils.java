package org.example.camunda.utils;

import io.camunda.zeebe.client.api.worker.JobWorker;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.example.camunda.core.actions.Action;
import org.example.camunda.dto.InstanceContext;
import org.example.camunda.dto.Scenario;
import org.example.camunda.dto.TimePrecisionEnum;

public class ContextUtils {

  private static List<String> histo = new ArrayList<>();

  private static int idleTimeBeforeClockMove = 300;
  private static Map<String, String> dateTimers = new HashMap<>();
  private static Map<String, String> durationTimers = new HashMap<>();
  private static SortedMap<Long, List<Action>> timedActions = new TreeMap<>();
  private static Map<Long, InstanceContext> processInstanceScenarioMap = new HashMap<>();
  private static Set<String> minHandledMap = new HashSet<>();
  private static Set<String> maxHandledMap = new HashSet<>();
  private static Set<Long> minProcessInstances = new HashSet<>();
  private static Set<Long> maxProcessInstances = new HashSet<>();
  private static List<JobWorker> activeWorkers = new ArrayList<>();

  public static void addInstance(Long processInstanceKey, Scenario scenario, Double progress) {
    processInstanceScenarioMap.put(processInstanceKey, new InstanceContext(scenario, progress));
    prepareMinMax(processInstanceKey, scenario, progress);
  }

  private static synchronized void prepareMinMax(
      Long processInstanceKey, Scenario scenario, Double progress) {
    if (!minHandledMap.contains(scenario.getName() + progress)) {
      minHandledMap.add(scenario.getName() + progress);
      minProcessInstances.add(processInstanceKey);
    } else if (!maxHandledMap.contains(scenario.getName() + progress)) {
      maxHandledMap.add(scenario.getName() + progress);
      maxProcessInstances.add(processInstanceKey);
    }
  }

  public static Double getProgress(Long processInstanceKey) {
    return getContext(processInstanceKey).getProgress();
  }

  public static InstanceContext getContext(Long processInstanceKey) {
    return processInstanceScenarioMap.get(processInstanceKey);
  }

  public static void addWorker(JobWorker worker) {
    activeWorkers.add(worker);
  }

  public static void addAction(long time, Action action, TimePrecisionEnum timePrecision) {
    if (time < System.currentTimeMillis())
      timedActions.get(buildEntry(timePrecision.round(time))).add(action);
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

  public static void clean() {
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
  }

  public static boolean shouldComputeMin(Long processInstanceKey) {
    return minProcessInstances.contains(processInstanceKey);
  }

  public static boolean shouldComputeMax(Long processInstanceKey) {
    return maxProcessInstances.contains(processInstanceKey);
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
    return idleTimeBeforeClockMove;
  }

  public static void setIdleTimeBeforeClockMove(int idleTimeBeforeClockMove) {
    ContextUtils.idleTimeBeforeClockMove = idleTimeBeforeClockMove;
  }

  public static void addHisto(String comment) {
    histo.add(Instant.now().toString() + " : " + comment);
  }

  public static List<String> getHisto() {
    return histo;
  }
}