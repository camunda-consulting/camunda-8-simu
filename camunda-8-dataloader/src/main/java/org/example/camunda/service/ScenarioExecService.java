package org.example.camunda.service;

import io.camunda.operate.model.FlowNodeInstance;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.example.camunda.core.ZeebeService;
import org.example.camunda.core.actions.Action;
import org.example.camunda.core.actions.CompleteJobAction;
import org.example.camunda.core.actions.MessageAction;
import org.example.camunda.core.actions.StartInstancesAction;
import org.example.camunda.dto.ExecutionPlan;
import org.example.camunda.dto.InstanceContext;
import org.example.camunda.dto.Scenario;
import org.example.camunda.dto.StepActionEnum;
import org.example.camunda.dto.StepAdditionalAction;
import org.example.camunda.dto.StepExecPlan;
import org.example.camunda.utils.BpmnUtils;
import org.example.camunda.utils.ContextUtils;
import org.example.camunda.utils.HistoUtils;
import org.example.camunda.utils.ScenarioUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScenarioExecService {
  private static final Logger LOG = LoggerFactory.getLogger(ScenarioExecService.class);
  DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS Z");

  @Autowired private ZeebeService zeebeService;

  public DeploymentEvent deploy(String name, String bpmnXml) {
    return zeebeService.deploy(name, bpmnXml);
  }

  public void start(ExecutionPlan plan) {

    start(plan, null);
  }

  public void start(ExecutionPlan plan, String scenarioName) {
    ContextUtils.setPlan(plan);
    initClock(System.currentTimeMillis());
    // prepareTimerCatchEvents(plan);
    prepareInstances(plan, scenarioName);
    prepareWorkers(plan);
    execute();
  }

  /*private void prepareTimerCatchEvents(ExecutionPlan plan) {
    Map<String, String> timers = BpmnUtils.getTimerCatchEvents(plan.getXml());
    for (Map.Entry<String, String> timer : timers.entrySet()) {
      String flowNodeId = timer.getKey();
      String wait = timer.getValue();
      if (wait.startsWith("date:")) {
        ContextUtils.addDateTimer(flowNodeId, wait.substring(5));
      } else {
        ContextUtils.addDurationTimer(flowNodeId, wait.substring(9));
      }
    }
  }*/

  private void prepareWorkers(ExecutionPlan plan) {
    List<String> jobTypes = BpmnUtils.getJobTypes(plan.getXml());
    for (String jobType : jobTypes) {
      ContextUtils.addWorker(
          this.zeebeService.createStreamingWorker(
              jobType,
              new JobHandler() {
                @Override
                public void handle(JobClient client, ActivatedJob job) throws Exception {
                  zeebeService.zeebeWorks();
                  Map<String, Object> variables = job.getVariablesAsMap();
                  String processUniqueId = (String) variables.get("uniqueProcessIdentifier");

                  InstanceContext context = ContextUtils.getContext(processUniqueId);
                  /*if (context==null) {
                      Map<String, Object> variables = job.getVariablesAsMap();
                      if (variables.containsKey("uniqueMsgIdentifier")) {
                      context =
                      }
                  }
                  while (context == null) {
                    ThreadUtils.pause(100);
                    context = ContextUtils.getContext(processInstanceKey);
                  }*/
                  StepExecPlan step = context.getScenario().getSteps().get(job.getElementId());
                  if (step.getPreSteps() != null) {
                    for (StepAdditionalAction preStep : step.getPreSteps()) {
                      if (preStep.getType() == StepActionEnum.CLOCK) {
                        ContextUtils.buildEntry(ContextUtils.getEngineTime() + preStep.getDelay());
                      } else if (preStep.getType() == StepActionEnum.MSG) {
                        ContextUtils.addAction(
                            ContextUtils.getEngineTime() + preStep.getDelay(),
                            new MessageAction(
                                preStep.getMsg(),
                                (String) variables.get(preStep.getCorrelationKey()),
                                preStep.getJsonTemplate(),
                                job.getVariablesAsMap(),
                                zeebeService),
                            context.getScenario().getTimePrecision());
                      }
                    }
                  }
                  if (step.getAction() == StepActionEnum.COMPLETE) {
                    long targetTime =
                        ContextUtils.getEngineTime()
                            + ScenarioUtils.calculateTaskDuration(step, processUniqueId);
                    targetTime =
                        ContextUtils.addAction(
                            targetTime,
                            new CompleteJobAction(
                                job.getKey(),
                                step.getJsonTemplate(),
                                job.getVariablesAsMap(),
                                zeebeService),
                            context.getScenario().getTimePrecision());
                    if (step.getPostSteps() != null) {
                      for (StepAdditionalAction postStep : step.getPostSteps()) {
                        if (postStep.getType() == StepActionEnum.CLOCK) {
                          ContextUtils.buildEntry(targetTime + postStep.getDelay());
                        } else if (postStep.getType() == StepActionEnum.MSG) {
                          ContextUtils.addAction(
                              targetTime + postStep.getDelay(),
                              new MessageAction(
                                  postStep.getMsg(),
                                  (String) variables.get(postStep.getCorrelationKey()),
                                  postStep.getJsonTemplate(),
                                  job.getVariablesAsMap(),
                                  zeebeService),
                              context.getScenario().getTimePrecision());
                        }
                      }
                    }
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
        s.setInstanceDistribution(plan.getInstanceDistribution());
        s.setTimePrecision(plan.getTimePrecision());
        prepareInstances(s);
      }
    }
  }

  private void prepareInstances(Scenario scenario) {
    ZonedDateTime firstDay = ScenarioUtils.getZonedDateDay(scenario.getFirstDayFeelExpression());
    ZonedDateTime lastDay = ScenarioUtils.getZonedDateDay(scenario.getLastDayFeelExpression());

    long time = firstDay.withHour(scenario.getDayTimeStart()).toEpochSecond() * 1000;

    long durationInDays = ChronoUnit.DAYS.between(firstDay, lastDay) + 1;
    for (double x = 0; x < durationInDays; x++) {
      long nbInstancesPerDay = ScenarioUtils.calculateInstancesPerDay(scenario, x / durationInDays);
      addInstancesWithDesiredPrecision(time, nbInstancesPerDay, scenario, x / durationInDays);

      time = time + 86400000;
    }
  }

  private void addInstancesWithDesiredPrecision(
      long time, long nbInstancesPerDay, Scenario scenario, double progress) {
    if (scenario.getInstanceDistribution() == ChronoUnit.DAYS) {
      ContextUtils.addAction(
          time,
          new StartInstancesAction(scenario, nbInstancesPerDay, this.zeebeService, progress),
          scenario.getTimePrecision());
    } else if (scenario.getInstanceDistribution() == ChronoUnit.HALF_DAYS) {
      ContextUtils.addAction(
          time,
          new StartInstancesAction(scenario, nbInstancesPerDay / 2, this.zeebeService, progress),
          scenario.getTimePrecision());
      int nextround = (scenario.getDayTimeEnd() - scenario.getDayTimeStart()) / 2;
      ContextUtils.addAction(
          time + nextround * ChronoUnit.HOURS.getDuration().toMillis(),
          new StartInstancesAction(scenario, nbInstancesPerDay / 2, this.zeebeService, progress),
          scenario.getTimePrecision());

    } else if (scenario.getInstanceDistribution() == ChronoUnit.HOURS) {
      int nbHours = scenario.getDayTimeEnd() - scenario.getDayTimeStart();
      long nbInstancesPerHour = nbInstancesPerDay / nbHours;
      long residual = nbInstancesPerDay - nbInstancesPerHour * nbHours;
      for (int i = 0; i < nbHours; i++) {
        long nbInstances = nbInstancesPerHour + (residual-- > 0 ? 1 : 0);
        if (nbInstances > 0) {
          ContextUtils.addAction(
              time + i * ChronoUnit.HOURS.getDuration().toMillis(),
              new StartInstancesAction(scenario, nbInstances, this.zeebeService, progress),
              scenario.getTimePrecision());
        }
      }
    } else if (scenario.getInstanceDistribution() == ChronoUnit.MINUTES) {
      int nbMinutes = (scenario.getDayTimeEnd() - scenario.getDayTimeStart()) * 60;
      long nbInstancesPerMinute = nbInstancesPerDay / nbMinutes;
      long residual = nbInstancesPerDay - nbInstancesPerMinute * nbMinutes;
      long residualDistrib = nbMinutes / residual;
      for (int i = 0; i < nbMinutes; i++) {
        long nbInstances = nbInstancesPerMinute + (i % residualDistrib == 0 ? 1 : 0);
        if (nbInstances > 0) {
          ContextUtils.addAction(
              time + i * ChronoUnit.MINUTES.getDuration().toMillis(),
              new StartInstancesAction(scenario, nbInstances, this.zeebeService, progress),
              scenario.getTimePrecision());
        }
      }
    }
  }

  public void initClock(Long clock) {
    ContextUtils.setEngineTime(this.zeebeService.setClock(clock));
  }

  public void execute() {
    initClock(ContextUtils.nextTimeEntry());

    this.zeebeService.waitEngineToBeIdle(
        () -> {
          nextTimedAction();
        });
  }

  public void stop() {
    running = false;
    HistoUtils.addHisto("Plan execution stopped");
    ContextUtils.endPlan();
    zeebeService.deleteControlledClock();
  }

  public void resume() {}

  boolean running = false;

  public synchronized void nextTimedAction() {
    if (!running) {
      running = true;
      if (ContextUtils.hasTimeEntries()) {
        Long timedKey = ContextUtils.nextTimeEntry();
        initClock(timedKey);
        HistoUtils.updateProgress(ContextUtils.nbEntries());
        HistoUtils.addHisto("Move clock to " + Instant.ofEpochMilli(timedKey).toString());
        List<Action> actions = ContextUtils.getActionsAt(timedKey);
        while (actions.size() > 0) {
          Action a = actions.get(0);
          a.run();
          actions.remove(a);
        }
        LOG.info("Actions at " + dateFormat.format(new Date(timedKey)) + " are executed");
        ContextUtils.removeTimeEntry(timedKey);
      } else {
        stop();
      }
    }
    running = false;
  }

  public void handleIntermediateEvent(FlowNodeInstance instance) {
    // TODO : test
    String flowNodeId = instance.getFlowNodeId();
    Long startTime = instance.getStartDate().getTime();
    Long targetTime = ScenarioUtils.getTimerCatchEventTime(flowNodeId, startTime);

    handleIntermediateEvent(targetTime);
  }

  public void handleIntermediateEvent(long dueTimestamp) {
    this.zeebeService.zeebeWorks();
    ContextUtils.buildEntry(dueTimestamp);
  }
}
