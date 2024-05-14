package org.example.camunda.service;

import io.camunda.operate.model.FlowNodeInstance;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobWorker;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.example.camunda.core.IntermediateCatchScheduler;
import org.example.camunda.core.ZeebeService;
import org.example.camunda.core.actions.Action;
import org.example.camunda.core.actions.CompleteJobAction;
import org.example.camunda.core.actions.StartInstancesAction;
import org.example.camunda.dto.ExecutionPlan;
import org.example.camunda.dto.Scenario;
import org.example.camunda.dto.ScenarioProgressionEnum;
import org.example.camunda.dto.StepActionEnum;
import org.example.camunda.dto.StepExecPlan;
import org.example.camunda.utils.BpmnUtils;
import org.example.camunda.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ScenarioExecService {
  private static final Logger LOG = LoggerFactory.getLogger(ScenarioExecService.class);
  DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS Z");

  private long estimateEngineTime;
  private SortedMap<Long, List<Action>> timedActions = new TreeMap<>();
  Map<Long, Scenario> processInstanceScenarioMap = new HashMap<>();
  List<JobWorker> activeWorkers = new ArrayList<>();

  @Value("${timers.from:operate}")
  private String timers;

  @Autowired private ZeebeService zeebeService;
  @Autowired private IntermediateCatchScheduler icScheduler;

  public DeploymentEvent deploy(String name, String bpmnXml) {
    return zeebeService.deploy(name, bpmnXml);
  }

  public Scenario getScenario(ExecutionPlan plan, String scenarioName) {
    for (Scenario s : plan.getScenarii()) {
      if (s.getName().equals(scenarioName)) {
        return s;
      }
    }
    return null;
  }

  public void start(ExecutionPlan plan) {
    start(plan, null);
  }

  public void start(ExecutionPlan plan, String scenarioName) {
    prepareInstances(plan, scenarioName);
    prepareWorkers(plan);
    execute();
  }

  private void prepareWorkers(ExecutionPlan plan) {
    List<String> jobTypes = BpmnUtils.getJobTypes(plan.getXml());
    for (String jobType : jobTypes) {
      activeWorkers.add(
          this.zeebeService.createStreamingWorker(
              jobType,
              new JobHandler() {
                @Override
                public void handle(JobClient client, ActivatedJob job) throws Exception {
                  zeebeService.zeebeWorks();
                  Long processInstanceKey = job.getProcessInstanceKey();
                  Scenario scenario = processInstanceScenarioMap.get(processInstanceKey);
                  while (scenario == null) {
                    ThreadUtils.pause(200);
                    scenario = processInstanceScenarioMap.get(processInstanceKey);
                  }
                  StepExecPlan step = scenario.getSteps().get(job.getElementId());
                  if (step.getAction() == StepActionEnum.COMPLETE) {
                    addAction(
                        estimateEngineTime + step.getAvgDuration(),
                        new CompleteJobAction(
                            job.getKey(),
                            step.getJsonTemplate(),
                            job.getVariablesAsMap(),
                            zeebeService));
                  }
                }
              }));
    }
  }

  private void prepareInstances(ExecutionPlan plan, String scenarioName) {
    for (Scenario s : plan.getScenarii()) {
      if (scenarioName == null || s.getName().equals(scenarioName)) {
        s.setBpmnProcessId(plan.getDefinition().getBpmnProcessId());
        s.setVersion(plan.getDefinition().getVersion());
        s.setPrecision(plan.getPrecision());
        prepareInstances(s);
      }
    }
  }

  private void prepareInstances(Scenario scenario) {
    long time = scenario.getFirstDay().withHour(scenario.getDayTimeStart()).toEpochSecond() * 1000;

    long durationInDays =
        ChronoUnit.DAYS.between(scenario.getFirstDay(), scenario.getLastDay()) + 1;
    for (double x = 0; x < durationInDays; x++) {
      long nbInstancesPerDay = calculateInstancesPerDay(scenario, x / durationInDays);
      addInstancesWithDesiredPrecision(time, nbInstancesPerDay, scenario);

      time = time + 86400000;
    }
  }

  private void addInstancesWithDesiredPrecision(
      long time, long nbInstancesPerDay, Scenario scenario) {
    if (scenario.getPrecision() == ChronoUnit.DAYS) {
      addAction(
          time,
          new StartInstancesAction(
              scenario, nbInstancesPerDay, this.zeebeService, processInstanceScenarioMap));
    } else if (scenario.getPrecision() == ChronoUnit.HALF_DAYS) {
      addAction(
          time,
          new StartInstancesAction(
              scenario, nbInstancesPerDay / 2, this.zeebeService, processInstanceScenarioMap));
      int nextround = (scenario.getDayTimeEnd() - scenario.getDayTimeStart()) / 2;
      addAction(
          time + nextround * ChronoUnit.HOURS.getDuration().toMillis(),
          new StartInstancesAction(
              scenario, nbInstancesPerDay / 2, this.zeebeService, processInstanceScenarioMap));

    } else if (scenario.getPrecision() == ChronoUnit.HOURS) {
      int nbHours = scenario.getDayTimeEnd() - scenario.getDayTimeStart();
      long nbInstancesPerHour = nbInstancesPerDay / nbHours;
      long residual = nbInstancesPerDay - nbInstancesPerHour * nbHours;
      for (int i = 0; i < nbHours; i++) {
        long nbInstances = nbInstancesPerHour + (residual-- > 0 ? 1 : 0);
        if (nbInstances > 0) {
          addAction(
              time + i * ChronoUnit.HOURS.getDuration().toMillis(),
              new StartInstancesAction(
                  scenario, nbInstances, this.zeebeService, processInstanceScenarioMap));
        }
      }
    } else if (scenario.getPrecision() == ChronoUnit.MINUTES) {
      int nbMinutes = (scenario.getDayTimeEnd() - scenario.getDayTimeStart()) * 60;
      long nbInstancesPerMinute = nbInstancesPerDay / nbMinutes;
      long residual = nbInstancesPerDay - nbInstancesPerMinute * nbMinutes;
      long residualDistrib = nbMinutes / residual;
      for (int i = 0; i < nbMinutes; i++) {
        long nbInstances = nbInstancesPerMinute + (i % residualDistrib == 0 ? 1 : 0);
        if (nbInstances > 0) {
          addAction(
              time + i * ChronoUnit.MINUTES.getDuration().toMillis(),
              new StartInstancesAction(
                  scenario, nbInstances, this.zeebeService, processInstanceScenarioMap));
        }
      }
    }
  }

  private long calculateInstancesPerDay(Scenario scenario, double progress) {

    int difference = scenario.getNbInstancesEnd() - scenario.getNbInstancesStart();
    if (scenario.getEvolution() == ScenarioProgressionEnum.LINEAR) {
      return scenario.getNbInstancesStart() + Math.round(difference * progress);
    }
    if (scenario.getEvolution() == ScenarioProgressionEnum.LINEAR_SALTED) {
      long saltDif = scenario.getSaltMax() - scenario.getSaltMin();
      long salt = Math.round(Math.random() * saltDif) + scenario.getSaltMin();
      long multiplier = (Math.round(Math.random()) < 1) ? -1 : 1;
      return scenario.getNbInstancesStart() + Math.round(difference * progress) + salt * multiplier;
    }
    return 0;
  }

  public void initClock(Long clock) {
    estimateEngineTime = this.zeebeService.setClock(clock);
  }

  public void addAction(long time, Action action) {
    timedActions.get(buildEntry(time)).add(action);
  }

  public long buildEntry(long time) {
    if (!timedActions.containsKey(time)) {
      timedActions.put(time, new ArrayList<>());
    }
    return time;
  }

  public void execute() {
    initClock(timedActions.firstKey());
    if (timers.equals("operate")) {
      icScheduler.start(this);
    }

    this.zeebeService.waitEngineToBeIdle(
        () -> {
          nextTimedAction();
        });
  }

  public void nextTimedAction() {
    if (!timedActions.isEmpty()) {
      Long timedKey = timedActions.firstKey();
      initClock(timedKey);
      List<Action> actions = timedActions.get(timedKey);
      while (actions.size() > 0) {
        Action a = actions.get(0);
        a.run();
        actions.remove(a);
      }
      LOG.info("Actions at " + dateFormat.format(new Date(timedKey)) + " are executed");
      timedActions.remove(timedKey);
    } else {
      for (JobWorker worker : activeWorkers) {
        worker.close();
      }
      activeWorkers.clear();
    }
  }

  public void handleIntermediateEvent(FlowNodeInstance instance) {
    handleIntermediateEvent(
        instance.getProcessInstanceKey(),
        instance.getFlowNodeId(),
        instance.getStartDate().getTime());
  }

  public void handleIntermediateEvent(Long processInstanceKey, String flowNodeId, long startTime) {
    Scenario scenario = processInstanceScenarioMap.get(processInstanceKey);
    if (scenario == null) {
      return;
    }
    StepExecPlan step = scenario.getSteps().get(flowNodeId);
    if (step.getAction() == StepActionEnum.CLOCK) {
      buildEntry(startTime + step.getTimeAdvance());
    }
  }
}
