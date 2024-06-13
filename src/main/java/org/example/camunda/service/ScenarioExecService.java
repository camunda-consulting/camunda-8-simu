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
import org.example.camunda.core.actions.BpmnErrorAction;
import org.example.camunda.core.actions.CompleteJobAction;
import org.example.camunda.core.actions.IncidentJobAction;
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

    if (plan.getXmlModified() || plan.getDefinition().getVersion() < 0) {
      deploy(plan.getDefinition().getName(), plan.getXml());
      if (plan.getXmlDependencies() != null) {
        for (String dep : plan.getXmlDependencies().keySet()) {
          deploy(dep, plan.getXmlDependencies().get(dep));
        }
      }
    }
    start(plan, null);
  }

  public void start(ExecutionPlan plan, String scenarioName) {
    ContextUtils.setPlan(plan);
    initClock(System.currentTimeMillis());
    prepareInstances(plan, scenarioName);
    prepareWorkers(plan);
    execute();
  }

  private void prepareWorkers(ExecutionPlan plan) {
    List<String> jobTypes = BpmnUtils.getJobTypes(plan.getXml());
    if (plan.getXmlDependencies() != null) {
      for (String xml : plan.getXmlDependencies().values()) {
        jobTypes.addAll(BpmnUtils.getJobTypes(xml));
      }
    }
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

                  StepExecPlan step = context.getScenario().getSteps().get(job.getElementId());

                  if (step.getPreSteps() != null) {
                    for (StepAdditionalAction preStep : step.getPreSteps()) {
                      // pre steps are calculated after current engine time (before completion time)
                      addAdditionalStep(ContextUtils.getEngineTime(), job, preStep, context);
                    }
                  }
                  // main step action
                  if (step.getAction() == StepActionEnum.INCIDENT) {
                    long targetTime =
                        ContextUtils.getEngineTime()
                            + ScenarioUtils.calculateTaskDuration(step, processUniqueId);
                    targetTime =
                        ContextUtils.addAction(
                            targetTime,
                            new IncidentJobAction(job.getKey(), step.getIncident(), zeebeService));
                  } else if (step.getAction() == StepActionEnum.COMPLETE) {
                    long targetTime =
                        ContextUtils.getEngineTime()
                            + ScenarioUtils.calculateTaskDuration(step, processUniqueId);
                    targetTime =
                        ContextUtils.addAction(
                            targetTime,
                            new CompleteJobAction(
                                job.getKey(),
                                step.getJsonTemplate().getTemplate(),
                                job.getVariablesAsMap(),
                                zeebeService));
                    // post steps are only managed when main action will complete. Typically
                    // intermediate catch event coming after.
                    if (step.getPostSteps() != null) {
                      for (StepAdditionalAction postStep : step.getPostSteps()) {
                        // post steps are calculated after completion time
                        addAdditionalStep(targetTime, job, postStep, context);
                      }
                    }
                  }
                }
              }));
    }
  }

  public void addAdditionalStep(
      Long baseDate, ActivatedJob job, StepAdditionalAction step, InstanceContext context) {
    Map<String, Object> variables = job.getVariablesAsMap();
    if (step.getType() == StepActionEnum.CLOCK) {
      ContextUtils.buildEntry(ScenarioUtils.getEstimatedTime(baseDate, step.getFeelDelay()));
    } else if (step.getType() == StepActionEnum.MSG) {
      ContextUtils.addAction(
          baseDate + step.getMsgDelay(),
          new MessageAction(
              step.getMsg(),
              (String) variables.get(step.getCorrelationKey()),
              step.getJsonTemplate().getTemplate(),
              job.getVariablesAsMap(),
              zeebeService));
    } else if (step.getType() == StepActionEnum.BPMN_ERROR) {
      ContextUtils.addAction(
          baseDate + step.getErrorDelay(),
          new BpmnErrorAction(
              step.getErrorCode(),
              job.getKey(),
              step.getJsonTemplate().getTemplate(),
              job.getVariablesAsMap(),
              zeebeService));
    }
  }

  private void prepareInstances(ExecutionPlan plan, String scenarioName) {
    for (Scenario s : plan.getScenarii()) {
      if (scenarioName == null || s.getName().equals(scenarioName)) {
        s.setBpmnProcessId(plan.getDefinition().getBpmnProcessId());
        s.setVersion(plan.getDefinition().getVersion());
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
    if (ContextUtils.getInstanceDistribution() == ChronoUnit.DAYS) {
      ContextUtils.addAction(
          time, new StartInstancesAction(scenario, nbInstancesPerDay, this.zeebeService, progress));
    } else if (ContextUtils.getInstanceDistribution() == ChronoUnit.HALF_DAYS) {
      ContextUtils.addAction(
          time,
          new StartInstancesAction(scenario, nbInstancesPerDay / 2, this.zeebeService, progress));
      int nextround = (scenario.getDayTimeEnd() - scenario.getDayTimeStart()) / 2;
      ContextUtils.addAction(
          time + nextround * ChronoUnit.HOURS.getDuration().toMillis(),
          new StartInstancesAction(scenario, nbInstancesPerDay / 2, this.zeebeService, progress));

    } else if (ContextUtils.getInstanceDistribution() == ChronoUnit.HOURS) {
      int nbHours = scenario.getDayTimeEnd() - scenario.getDayTimeStart();
      long nbInstancesPerHour = nbInstancesPerDay / nbHours;
      long residual = nbInstancesPerDay - nbInstancesPerHour * nbHours;
      for (int i = 0; i < nbHours; i++) {
        long nbInstances = nbInstancesPerHour + (residual-- > 0 ? 1 : 0);
        if (nbInstances > 0) {
          ContextUtils.addAction(
              time + i * ChronoUnit.HOURS.getDuration().toMillis(),
              new StartInstancesAction(scenario, nbInstances, this.zeebeService, progress));
        }
      }
    } else if (ContextUtils.getInstanceDistribution() == ChronoUnit.MINUTES) {
      int nbMinutes = (scenario.getDayTimeEnd() - scenario.getDayTimeStart()) * 60;
      long nbInstancesPerMinute = nbInstancesPerDay / nbMinutes;
      long residual = nbInstancesPerDay - nbInstancesPerMinute * nbMinutes;
      long residualDistrib = nbMinutes / residual;
      for (long i = 0; i < nbMinutes; i++) {
        long nbInstances = nbInstancesPerMinute + (i % residualDistrib == 0 ? 1 : 0);
        if (nbInstances > 0) {
          ContextUtils.addAction(
              time + i * ChronoUnit.MINUTES.getDuration().toMillis(),
              new StartInstancesAction(scenario, nbInstances, this.zeebeService, progress));
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
