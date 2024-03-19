package org.example.camunda.core;

import io.camunda.operate.model.FlowNodeInstance;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.example.camunda.core.actions.Action;
import org.example.camunda.core.actions.CompleteJobAction;
import org.example.camunda.core.actions.StartInstancesAction;
import org.example.camunda.core.model.Scenario;
import org.example.camunda.core.model.Step;
import org.example.camunda.core.model.StepType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ScenarioExecutor {

  public static final long START_CLOCK = 1702298968000L;

  @Value("${timers.from:operate}")
  private String timers;

  @Autowired private ZeebeService zeebeService;
  @Autowired private IntermediateCatchScheduler icScheduler;
  private List<Scenario> scenarii = new ArrayList<>();
  Map<Long, Scenario> processInstanceScenarioMap = new HashMap<>();
  private long precision = 5000;
  private long estimateEngineTime;
  private SortedMap<Long, List<Action>> timedActions = new TreeMap<>();

  public void addScenario(Scenario scenario) {
    scenarii.add(scenario);
    Step firstStep = scenario.getSteps().get(0);
    if (firstStep.getType() == StepType.START) {
      addAction(
          firstStep.getTimeBeforeCompletion(),
          new StartInstancesAction(scenario, this.zeebeService, processInstanceScenarioMap));
    }
  }

  public long buildEntry(long time) {
    Long bulkTime = getBulkTime(time);
    if (!timedActions.containsKey(bulkTime)) {
      timedActions.put(bulkTime, new ArrayList<>());
    }
    return bulkTime;
  }

  public void addAction(long time, Action action) {
    timedActions.get(buildEntry(time)).add(action);
  }

  public void addFixedAction(long fixedTime, Action action) {
    addAction(fixedTime - START_CLOCK, action);
  }

  private long getBulkTime(long time) {
    return (long) (Math.floor(time / precision) * precision);
  }

  public void initClock() {
    estimateEngineTime = this.zeebeService.setClock(START_CLOCK);
  }

  public void moveCLock(long elapsedTime) {

    estimateEngineTime = this.zeebeService.moveClock(elapsedTime);
  }

  Long previousTimedKey = 0l;

  public void nextTimedActions() {
    if (!timedActions.isEmpty()) {
      Long timedKey = timedActions.firstKey();
      moveCLock(timedKey - previousTimedKey);
      List<Action> actions = timedActions.get(timedKey);
      while (actions.size() > 0) {
        Action a = actions.get(0);
        a.run();
        actions.remove(a);
      }
      previousTimedKey = timedKey;
      timedActions.remove(timedKey);
    }
  }

  public void execute() {
    initClock();
    if (timers.equals("operate")) {
      icScheduler.start(this);
    }

    this.zeebeService.waitEngineToBeIdle(
        () -> {
          nextTimedActions();
        });
  }

  public Scenario getScenario(long processInstanceKey) {
    return processInstanceScenarioMap.get(processInstanceKey);
  }

  public void executeWorker(ActivatedJob job, Map<String, Object> variables) throws IOException {
    this.zeebeService.zeebeWorks();
    Scenario scenario = getScenario(job.getProcessInstanceKey());
    if (scenario == null) {
      return;
    }
    Step step = scenario.getStep(job.getElementId());
    if (step.isShouldComplete()) {
      if (step.getTimeBeforeCompletion() == 0) {
        zeebeService.completeJob(
            job.getKey(), Action.getVariables(step.getTemplateVariables(), variables));
      } else {
        String created = (String) variables.get("created");
        ZonedDateTime ldt =
            LocalDateTime.parse(created, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                .atZone(ZoneId.of("GMT"));
        long creation = ldt.toInstant().toEpochMilli();
        CompleteJobAction action =
            new CompleteJobAction(
                job.getKey(), step.getTemplateVariables(), variables, this.zeebeService);
        addFixedAction(creation + step.getTimeBeforeCompletion(), action);
      }
    }
  }

  public void executeIntermediateEvent(FlowNodeInstance instance) {
    executeIntermediateEvent(
        instance.getProcessInstanceKey(),
        instance.getFlowNodeId(),
        instance.getStartDate().getTime());
  }

  public void executeIntermediateEvent(Long processInstanceKey, String flowNodeId, long startTime) {
    zeebeService.zeebeWorks();
    Scenario scenario = getScenario(processInstanceKey);
    if (scenario == null) {
      return;
    }
    Step step = scenario.getStep(flowNodeId);
    if (step.getType() == StepType.INTERMEDIATE_TIMER) {
      buildEntry(startTime + step.getTimeBeforeCompletion() - START_CLOCK);
    }
  }

  public void executeIntermediateEvent(Long processInstanceKey, String flowNodeId) {
    executeIntermediateEvent(processInstanceKey, flowNodeId, estimateEngineTime);
  }
}
