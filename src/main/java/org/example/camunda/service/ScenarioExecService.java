package org.example.camunda.service;

import static org.example.camunda.core.actions.Action.getVariables;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import org.example.camunda.Constants;
import org.example.camunda.core.ZeebeService;
import org.example.camunda.core.actions.Action;
import org.example.camunda.core.actions.BpmnErrorAction;
import org.example.camunda.core.actions.CompleteJobAction;
import org.example.camunda.core.actions.IncidentJobAction;
import org.example.camunda.core.actions.MessageAction;
import org.example.camunda.core.actions.SignalAction;
import org.example.camunda.dto.*;
import org.example.camunda.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScenarioExecService {
  private static final Logger LOG = LoggerFactory.getLogger(ScenarioExecService.class);

  @Autowired private ZeebeService zeebeService;

  private boolean running = false;

  public DeploymentEvent deploy(String name, String bpmnXml) {
    return zeebeService.deploy(name, bpmnXml);
  }

  public void deploy(ExecutionPlan plan) {
    // if (plan.getXmlModified() || plan.getDefinition().getVersion() < 0) {

    // Map<String, Integer> deployedVersion = new HashMap<>();
    if (plan.getXmlDependencies() != null) {
      for (String dep : plan.getXmlDependencies().keySet()) {
        String xml = plan.getXmlDependencies().get(dep);
        // TODO : issue on multilevel call activities to be fixed
        DeploymentEvent event = deploy(dep + ".bpmn", plan.getXmlDependencies().get(dep));
        // deployedVersion.put(dep, event.getProcesses().get(0).getVersion());
      }
    }
    // BpmnSimulatorModifUtils.updateCallActivityVersion(plan, deployedVersion);
    DeploymentEvent event = deploy(plan.getDefinition().getName() + ".bpmn", plan.getXml());
    // plan.getDefinition().setVersion((long) event.getProcesses().get(0).getVersion());

    // TODO : fixed call versions on call activities
    if (plan.getDmnDependencies() != null) {
      for (String dep : plan.getDmnDependencies().keySet()) {
        deploy(dep + ".dmn", plan.getDmnDependencies().get(dep));
      }
    }
  }

  public void start(ExecutionPlan plan) {
    start(plan, null);
  }

  public void start(ExecutionPlan plan, String scenarioName) {
    // DatasetService.getDatasets();
    // we deploy faked connectors and workers to not be disturbed by connectors and custom demo
    // workers
    BpmnSimulatorModifUtils.prepareSimulation(plan);
    deploy(plan);
    ContextUtils.setPlan(plan);
    HistoUtils.start();
    prepareWorkers(plan);
    // TODO: move to a reactive approach where we move to the next point in time when the engine is
    // quite for some time.
    new Thread(
            new Runnable() {
              @Override
              public void run() {

                startDailyInstances(plan, scenarioName);
                stop();
              }
            })
        .start();
  }

  private void prepareWorkers(ExecutionPlan plan) {
    List<String> jobTypes = BpmnUtils.getJobTypes(plan.getXml());
    if (plan.getXmlDependencies() != null) {
      for (String xml : plan.getXmlDependencies().values()) {
        jobTypes.addAll(BpmnUtils.getJobTypes(xml));
      }
    }
    ContextUtils.addWorker(
        this.zeebeService.createStreamingWorker(
            "processTerminated",
            new JobHandler() {
              @Override
              public void handle(JobClient client, ActivatedJob job) throws Exception {
                Map<String, Object> variables = job.getVariablesAsMap();
                String processUniqueId = (String) variables.get(Constants.UNIQUE_ID_KEY);
                ContextUtils.instanceCompleted(processUniqueId);
                client.newCompleteCommand(job.getKey()).send();
              }
            }));
    ContextUtils.addWorker(
        this.zeebeService.createStreamingWorker(
            "startEventListener",
            new JobHandler() {
              @Override
              public void handle(JobClient client, ActivatedJob job) throws Exception {
                Map<String, Object> variables = job.getVariablesAsMap();
                String processUniqueId = (String) variables.get(Constants.UNIQUE_ID_KEY);
                ContextUtils.addInstanceKey(processUniqueId, job.getProcessInstanceKey());
                client.newCompleteCommand(job.getKey()).send();
              }
            }));
    for (String jobType : jobTypes) {
      ContextUtils.addWorker(
          this.zeebeService.createStreamingWorker(
              jobType,
              new JobHandler() {
                @Override
                public void handle(JobClient client, ActivatedJob job) throws Exception {
                  if (ContextUtils.checkAlreadyReceived(job.getKey())) {
                    return;
                  }
                  Map<String, Object> variables = job.getVariablesAsMap();
                  String processUniqueId = (String) variables.get(Constants.UNIQUE_ID_KEY);
                  Long processInstanceTime = ContextUtils.getProcessInstanceTime(processUniqueId);

                  InstanceContext context = ContextUtils.getContext(processUniqueId);
                  if (context == null) {
                    LOG.error(
                        "Not retrieving context for "
                            + processUniqueId
                            + " for process instance "
                            + job.getProcessInstanceKey());
                  } else {
                    StepExecPlan step = context.getScenario().getSteps().get(job.getElementId());
                    if (step == null) {
                      LOG.error(
                          "Not retrieving step "
                              + job.getElementId()
                              + " for scenario"
                              + context.getScenario().getName()
                              + " related to process instance "
                              + job.getProcessInstanceKey()
                              + " and unique identifier "
                              + variables.get(Constants.UNIQUE_ID_KEY));
                      return;
                    }
                    if (step.getPreSteps() != null) {
                      for (StepAdditionalAction preStep : step.getPreSteps()) {
                        // pre steps are calculated after current engine time (before completion
                        // time)
                        addAdditionalStep(processInstanceTime, job, preStep);
                      }
                    }
                    // main step action
                    long mainTargetTime =
                        processInstanceTime + ScenarioUtils.getTaskDuration(step, context);
                    LOG.warn(
                        "Target time for "
                            + processUniqueId
                            + " at "
                            + step.getElementId()
                            + " : "
                            + mainTargetTime
                            + " (original time "
                            + processInstanceTime
                            + ")");
                    if (mainTargetTime > System.currentTimeMillis()) {
                      ContextUtils.instanceCompleted(processUniqueId);
                    } else {
                      if (step.getAction() == StepActionEnum.INCIDENT) {
                        ContextUtils.addAction(
                            mainTargetTime,
                            new IncidentJobAction(
                                job.getKey(),
                                step.getIncident(),
                                variables,
                                zeebeService,
                                mainTargetTime));
                      } else if (step.getAction() == StepActionEnum.COMPLETE) {
                        ContextUtils.addAction(
                            mainTargetTime,
                            new CompleteJobAction(
                                job.getKey(),
                                step.getJsonTemplate().getTemplate(),
                                job.getVariablesAsMap(),
                                zeebeService,
                                mainTargetTime));
                        // post steps are only managed when main action will complete. Typically
                        // intermediate catch event coming after.
                        if (step.getPostSteps() != null) {
                          for (StepAdditionalAction postStep : step.getPostSteps()) {
                            // post steps are calculated after completion time
                            addAdditionalStep(mainTargetTime, job, postStep);
                          }
                        }
                      }
                    }
                  }
                }
              }));
    }
  }

  public void addAdditionalStep(Long baseDate, ActivatedJob job, StepAdditionalAction step) {
    Map<String, Object> variables = job.getVariablesAsMap();
    String processUniqueId = (String) variables.get(Constants.UNIQUE_ID_KEY);
    long dateTarget = baseDate + ScenarioUtils.durationToMillis(step.getDelay());
    if (dateTarget > System.currentTimeMillis()) {
      ContextUtils.instanceCompleted(processUniqueId);
    } else {
      if (step.getType() == StepActionEnum.CLOCK) {
        ContextUtils.addTime(dateTarget, processUniqueId);
      } else if (step.getType() == StepActionEnum.MSG) {
        ContextUtils.addAction(
            dateTarget,
            new MessageAction(
                step.getMsg(),
                ScenarioUtils.getCorrelationKeyValue(variables, step.getCorrelationKey()),
                step.getJsonTemplate().getTemplate(),
                job.getVariablesAsMap(),
                zeebeService,
                dateTarget));
      } else if (step.getType() == StepActionEnum.SIGNAL) {
        ContextUtils.addAction(
            dateTarget,
            new SignalAction(
                step.getSignal(),
                step.getJsonTemplate().getTemplate(),
                job.getVariablesAsMap(),
                zeebeService,
                dateTarget));
      } else if (step.getType() == StepActionEnum.BPMN_ERROR) {
        ContextUtils.addAction(
            dateTarget,
            new BpmnErrorAction(
                step.getErrorCode(),
                job.getKey(),
                step.getJsonTemplate().getTemplate(),
                job.getVariablesAsMap(),
                zeebeService,
                dateTarget));
      }
    }
  }

  private void startDailyInstances(ExecutionPlan plan, String scenarioName) {
    running = true;
    for (Scenario s : plan.getScenarii()) {
      if (scenarioName == null || s.getName().equals(scenarioName)) {
        s.setBpmnProcessId(plan.getDefinition().getBpmnProcessId());
        s.setVersion(plan.getDefinition().getVersion());
        startDailyInstances(s);
      }
    }
  }

  private void startDailyInstances(Scenario scenario) {
    ZonedDateTime firstDay = ScenarioUtils.getZonedDateDay(scenario.getFirstDayFeelExpression());
    ZonedDateTime lastDay = ScenarioUtils.getZonedDateDay(scenario.getLastDayFeelExpression());

    long time = firstDay.withHour(scenario.getDayTimeStart()).toEpochSecond() * 1000;

    long durationInDays = ChronoUnit.DAYS.between(firstDay, lastDay) + 1;
    for (double x = 0; x < durationInDays && running; x++) {
      long nbInstancesPerDay =
          ScenarioUtils.calculateInstancesPerDay(scenario.getEvolution(), x / durationInDays);
      addInstancesWithDesiredPrecision(time, nbInstancesPerDay, scenario, x / durationInDays);

      time = time + 86400000;
    }
  }

  private void startAndCompleteInstances(
      long time, Scenario scenario, long nbInstances, double progress) {
    ThreadUtils.pause(100);
    setClock(time);
    startInstances(time, nbInstances, scenario, progress);
    long lastWork = System.currentTimeMillis();
    while (ContextUtils.getNbInstances() > 0 && running) {
      while (nextTimedAction()) {
        lastWork = System.currentTimeMillis();
      }
      if (ContextUtils.getNbInstances() > 0) {
        // detect connection loss with zeebe after 10s
        if (lastWork < System.currentTimeMillis() - 10000) {
          LOG.error("Zeebe connection issue. Reestablish connection.");
          ContextUtils.stopWorkers();
          zeebeService.zeebeClient(true);
          cancelPendingInstances();
          prepareWorkers(ContextUtils.getPlan());
          lastWork = System.currentTimeMillis();
        }
        // LOG.warn("PAUSING : no time entries but scenarios aren't completed");
        ThreadUtils.pause(100);
      }
    }
  }

  public void cancelPendingInstances() {
    for (Long pendingInstanceKey : ContextUtils.getPendingInstanceKeys()) {
      zeebeService.cancel(pendingInstanceKey);
    }
    ContextUtils.cleanPendingInstances();
    // zeebeService.cancelPendingInstances(ContextUtils.getPlan().getName());
  }

  private void startInstances(long time, long nbInstances, Scenario scenario, double progress) {
    Map<Long, Future<Boolean>> futures = new HashMap<>();

    for (long x = 0; x < nbInstances; x++) {
      futures.put(
          x,
          CompletableFuture.supplyAsync(
              () -> {
                ObjectNode variables = getVariables(scenario.getJsonTemplate().getTemplate(), null);
                String uniqueId = UUID.randomUUID().toString();
                ContextUtils.addInstance(uniqueId, scenario, progress);
                ContextUtils.setProcessInstanceTime(uniqueId, time);
                variables.put(Constants.UNIQUE_ID_KEY, uniqueId);
                if (InstanceStartTypeEnum.MSG == scenario.getStartType()) {
                  this.zeebeService.message(scenario.getStartMsgName(), uniqueId, variables);
                } else {
                  this.zeebeService.startProcessInstance(
                      scenario.getBpmnProcessId(), scenario.getVersion(), variables);
                }

                return true;
              }));
    }

    try {
      for (Map.Entry<Long, Future<Boolean>> varFutures : futures.entrySet()) {
        varFutures.getValue().get();
      }
    } catch (ExecutionException | InterruptedException e) {
      LOG.error("Error executing actions", e);
    }
    HistoUtils.startInstances(nbInstances);
    ContextUtils.setNbInstances(nbInstances);
  }

  private void addInstancesWithDesiredPrecision(
      long time, long nbInstancesPerDay, Scenario scenario, double progress) {
    if (ContextUtils.getInstanceDistribution() == ChronoUnit.DAYS) {
      // ContextUtils.addInstancesToStart(time, scenario, nbInstancesPerDay, progress);
      startAndCompleteInstances(time, scenario, nbInstancesPerDay, progress);
    } else if (ContextUtils.getInstanceDistribution() == ChronoUnit.HALF_DAYS) {
      // ContextUtils.addInstancesToStart(time, scenario, nbInstancesPerDay / 2, progress);
      startAndCompleteInstances(time, scenario, nbInstancesPerDay / 2, progress);
      int nextround = (scenario.getDayTimeEnd() - scenario.getDayTimeStart()) / 2;
      // ContextUtils.addInstancesToStart(time + nextround *
      // ChronoUnit.HOURS.getDuration().toMillis(), scenario, nbInstancesPerDay / 2, progress);
      startAndCompleteInstances(
          time + nextround * ChronoUnit.HOURS.getDuration().toMillis(),
          scenario,
          nbInstancesPerDay / 2,
          progress);
    } else if (ContextUtils.getInstanceDistribution() == ChronoUnit.HOURS) {
      int nbHours = scenario.getDayTimeEnd() - scenario.getDayTimeStart();
      long nbInstancesPerHour = nbInstancesPerDay / nbHours;
      long residual = nbInstancesPerDay - nbInstancesPerHour * nbHours;
      for (int i = 0; i < nbHours; i++) {
        long nbInstances = nbInstancesPerHour + (residual-- > 0 ? 1 : 0);
        if (nbInstances > 0) {
          // ContextUtils.addInstancesToStart(time + i * ChronoUnit.HOURS.getDuration().toMillis(),
          // scenario, nbInstances, progress);
          startAndCompleteInstances(
              time + i * ChronoUnit.HOURS.getDuration().toMillis(),
              scenario,
              nbInstances,
              progress);
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
          // ContextUtils.addInstancesToStart(time + i *
          // ChronoUnit.MINUTES.getDuration().toMillis(), scenario, nbInstances, progress);
          startAndCompleteInstances(
              time + i * ChronoUnit.MINUTES.getDuration().toMillis(),
              scenario,
              nbInstances,
              progress);
        }
      }
    }
  }

  public synchronized void setClock(Long clock) {
    if (clock < ContextUtils.getEngineTime()) {
      LOG.error("Moving clock back in time from " + ContextUtils.getEngineTime() + " to " + clock);
    }
    this.zeebeService.setClock(clock);
  }

  private void pauseBasedOnNextTimeDiff() {
    long diff = Math.abs(ContextUtils.nextTimeEntry() - ContextUtils.getEngineTime());
    if (diff < 1000) {
      // no wait
    } else if (diff < 2000) {
      ThreadUtils.pause(40);
    } else if (diff < 10000) {
      ThreadUtils.pause(80);
    } else if (diff < 60000) {
      ThreadUtils.pause(120);
    } else if (diff < 300000) {
      ThreadUtils.pause(140);
    } else if (diff < 360000) {
      ThreadUtils.pause(160);
    } else if (diff < 720000) {
      ThreadUtils.pause(200);
    } else if (diff < 1440000) {
      ThreadUtils.pause(300);
    } else if (diff < 2880000) {
      LOG.warn(
          "Moving from " + ContextUtils.getEngineTime() + " to " + ContextUtils.nextTimeEntry());
      ThreadUtils.pause(500);
    } else {
      LOG.error(
          "Moving from " + ContextUtils.getEngineTime() + " to " + ContextUtils.nextTimeEntry());
      ThreadUtils.pause(800);
    }
  }

  public synchronized boolean nextTimedAction() {
    if (ContextUtils.hasTimeEntries()) {
      // pauseBasedOnNextTimeDiff();
      // ThreadUtils.waitEngineIdle();
      ThreadUtils.pause(20);
      long timedKey = ContextUtils.nextTimeEntry();
      setClock(timedKey);
      while (!ContextUtils.isEmptyTime(timedKey)) {
        executeActionAtTime(timedKey, "Clock");
        executeActionAtTime(timedKey, "Incident");
        executeActionAtTime(timedKey, "BpmnError");
        executeActionAtTime(timedKey, "Complete");
        executeActionAtTime(timedKey, "Message");
        executeActionAtTime(timedKey, "Signal");
      }
      ContextUtils.removeTimeEntry(timedKey);
      return true;
    }
    return false;
  }

  public void executeActionAtTime(long time, String actionType) {
    // avoid concurrent modif exception
    List<Action> actionList = ContextUtils.getActionsAt(time, actionType);
    if (actionList == null || actionList.isEmpty()) {
      return;
    }
    List<Action> actions = List.copyOf(actionList);

    while (actions != null && !actions.isEmpty()) {
      ThreadUtils.execute(actions, true);
      actionList.removeAll(actions);
      actions = List.copyOf(actionList); // pick new tasks bein added at this point in time
    }
  }

  public void stop() {
    LOG.info("STOP PLAN EXEC");
    running = false;
    ExecutionPlan plan = ContextUtils.getPlan();
    HistoUtils.stop();
    zeebeService.stop();
    ContextUtils.endPlan();
    /* Deploy original files again to let custom demo work as expected */
    BpmnSimulatorModifUtils.revertToInitial(plan);
    deploy(plan);
  }
}
