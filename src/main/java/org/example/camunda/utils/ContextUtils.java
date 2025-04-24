package org.example.camunda.utils;

import io.camunda.zeebe.client.api.worker.JobWorker;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Getter;
import net.bytebuddy.asm.MemberSubstitution;
import org.example.camunda.core.actions.Action;
import org.example.camunda.core.actions.ClockAction;
import org.example.camunda.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextUtils {

  private static final Logger LOG = LoggerFactory.getLogger(ContextUtils.class);

  private static long estimateEngineTime;
  private static boolean executing = true;
  private static ExecutionPlan currentPlan = null;
  private static final SortedMap<Long, Map<String, InstancesToStart>> timedScenarioInstance =
      new TreeMap<>();
  private static final SortedMap<Long, Map<String, List<Action>>> timedActions = new TreeMap<>();
  private static final Map<String, InstanceContext> processInstanceScenarioMap = new HashMap<>();
  private static final Map<String, Long> uniqueKeyToInstanceKey = new HashMap<>();
  private static final List<JobWorker> activeWorkers = new ArrayList<>();

  @Getter
  private static final Map<String, List<StepExecPlan>> intermediateClockCalculations = new HashMap<>();

  public static void addInstancesToStart(
      long time, Scenario scenario, Long nbInstances, double progress) {
    if (!timedScenarioInstance.containsKey(time)) {
      timedScenarioInstance.put(time, new HashMap<>());
    }
    timedScenarioInstance
        .get(time)
        .put(scenario.getName(), new InstancesToStart(time, scenario, progress, nbInstances));
  }

  public static InstancesToStart getInstancesToStart() {
    if (timedScenarioInstance.isEmpty()) {
      return null;
    }
    return timedScenarioInstance.firstEntry().getValue().values().iterator().next();
  }

  public static void started(long time, String scenario) {
    timedScenarioInstance.get(time).remove(scenario);
    if (timedScenarioInstance.get(time).isEmpty()) {
      timedScenarioInstance.remove(time);
    }
  }

  public static synchronized void addInstance(String uniqueId, Scenario scenario, Double progress) {
    processInstanceScenarioMap.put(uniqueId, new InstanceContext(scenario, uniqueId, progress));
  }

  public static InstanceContext getContext(String processUniqueId) {
    return processInstanceScenarioMap.get(processUniqueId);
  }

  public static void addWorker(JobWorker worker) {
    activeWorkers.add(worker);
  }

  public static void addTime(long time, String uniqueId) {
    List<Action> existing = getActionsAt(time, "Clock");
    if (existing != null && !existing.isEmpty()) {
      ((ClockAction) (existing.get(0))).addUniqueId(uniqueId);
    } else {
      addAction(time, new ClockAction(uniqueId, time));
    }
  }

  public static synchronized void addDeleteTimedAction(long time, boolean delete) {
    if (delete && timedActions.containsKey(time)) {
      timedActions.remove(time);
    } else if (!delete) {
      if (!timedActions.containsKey(time)) {
        timedActions.put(time, new HashMap<>());
      }
    }
  }

  public static synchronized long addAction(long realTime, Action action) {
    addDeleteTimedAction(realTime, false);
    if (action != null) {
      if (!timedActions.get(realTime).containsKey(action.getType())) {
        timedActions.get(realTime).put(action.getType(), new ArrayList<>());
      }
      timedActions.get(realTime).get(action.getType()).add(action);
    }
    return realTime;
  }

  public static List<Action> getActionsAt(long time, String type) {
    if (timedActions.containsKey(time)) {
      return timedActions.get(time).get(type);
    } else {
      return null;
    }
  }

  public static long nextTimeEntry() {
    return timedActions.firstKey();
  }

  public static boolean isEmptyTime(long time) {
    try {
      for (List<Action> actions : timedActions.get(time).values()) {
        if (!actions.isEmpty()) {
          return false;
        }
      }
    } catch (NullPointerException e) {

    }
    return true;
  }

  public static void removeTimeEntry(long time) {
    addDeleteTimedAction(time, true);
  }

  public static boolean hasTimeEntries() {
    return !timedActions.isEmpty();
  }

  public static void stopWorkers() {
    for (JobWorker worker : activeWorkers) {
      worker.close();
    }
    activeWorkers.clear();
  }

  public static void endPlan() {
    stopWorkers();
    // HistoUtils.endPlan();
    executing = false;
    timedActions.clear();
    estimateEngineTime = 0;
    cleanPendingInstances();
  }

  public static void setPlan(ExecutionPlan plan) {
    currentPlan = plan;
    executing = true;
  }

  public static ExecutionPlan getPlan() {
    return currentPlan;
  }

  public static boolean isExecuting() {
    return executing;
  }

  public static long getEngineTime() {
    return estimateEngineTime;
  }

  public static void setEngineTime(long time) {
    estimateEngineTime = time;
  }

  public static ChronoUnit getInstanceDistribution() {
    return currentPlan.getInstanceDistribution();
  }

  private static AtomicLong nbInstances = new AtomicLong(0);

  public static synchronized void setNbInstances(long newInstances) {
    nbInstances = new AtomicLong(newInstances);
  }

  public static Long getNbInstances() {
    return nbInstances.get();
  }

  public static void instanceResolved(String processUniqueId, boolean completed) {
    if (completed) {
      processInstanceScenarioMap.remove(processUniqueId);
      mapTaskDuration.remove(processUniqueId);
      processInstanceTime.remove(processUniqueId);
    }
    uniqueKeyToInstanceKey.remove(processUniqueId);
    nbInstances.decrementAndGet();
    HistoUtils.completeInstances(1);
  }

  private static final Map<String, Map<String, Long>> mapTaskDuration = new HashMap<>();

  public static void addTaskDuration(String processUniqueId, String stepId, Long duration) {
    if (!mapTaskDuration.containsKey(processUniqueId)) {
      mapTaskDuration.put(processUniqueId, new HashMap<>());
    }
    mapTaskDuration.get(processUniqueId).put(stepId, duration);
  }

  public static Long getTaskDuration(String processUniqueId, String stepId) {
    if (!mapTaskDuration.containsKey(processUniqueId)) {
      return null;
    }
    return mapTaskDuration.get(processUniqueId).get(stepId);
  }

  public static final Map<String, Long> processInstanceTime = new HashMap<>();

  public static void setProcessInstanceTime(String processUniqueId, long time) {
    if (!processInstanceTime.containsKey(processUniqueId)
        || processInstanceTime.get(processUniqueId) < time) {
      processInstanceTime.put(processUniqueId, time);
    }
  }

  public static Long getProcessInstanceTime(String processUniqueId) {
    return processInstanceTime.get(processUniqueId);
  }

  public static void addInstanceKey(String processUniqueId, long processInstanceKey) {
    uniqueKeyToInstanceKey.put(processUniqueId, processInstanceKey);
  }

  public static Collection<Long> getPendingInstanceKeys() {
    return uniqueKeyToInstanceKey.values();
  }

  public static void cleanPendingInstances() {
    processInstanceScenarioMap.clear();
    mapTaskDuration.clear();
    processInstanceTime.clear();
    nbInstances.set(0);
    uniqueKeyToInstanceKey.clear();
    jobKeysReceived.clear();
  }

  private static final Set<Long> jobKeysReceived = new HashSet<>();

  public static synchronized boolean checkAlreadyReceived(Long key) {
    boolean already = jobKeysReceived.contains(key);
    if (already) return true;
    jobKeysReceived.add(key);
    return false;
  }

  private static final Map<String, Long> avgStepDuration = new HashMap<>();

  public static void addAvgStepDuration(
      String step, String scenario, Double progress, Long duration) {
    avgStepDuration.put(step + scenario + progress, duration);
  }

  public static Long getAvgStepDuration(String step, String scenario, Double progress) {
    return avgStepDuration.get(step + scenario + progress);
  }


  public static void addIntermediateClockCalculation(String processUniqueId, StepExecPlan step) {
    addDeleteClock(processUniqueId, step);
  }

  public static void removeIntermediateClockCalculation(Collection<String> processUniqueIds) {
    for(String id : processUniqueIds) {
      addDeleteClock(id, null);
    }
  }
  //if step is null, the entry will be deleted
  private static synchronized void addDeleteClock(String processUniqueId, StepExecPlan step) {
    if (step==null) {
      intermediateClockCalculations.remove(processUniqueId);
    } else {
      if (!intermediateClockCalculations.containsKey(processUniqueId)) {
        intermediateClockCalculations.put(processUniqueId, new ArrayList<>());
      }
      intermediateClockCalculations.get(processUniqueId).add(step);
    }
  }
}
