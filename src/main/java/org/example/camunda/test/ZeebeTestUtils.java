package org.example.camunda.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.process.test.engine.EngineFactory;
import io.camunda.zeebe.process.test.engine.InMemoryEngine;
import io.camunda.zeebe.protocol.impl.record.value.incident.IncidentRecord;
import io.camunda.zeebe.protocol.impl.record.value.processinstance.ProcessInstanceRecord;
import io.camunda.zeebe.protocol.impl.record.value.variable.VariableRecord;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.camunda.zeebe.protocol.record.intent.VariableIntent;
import io.camunda.zeebe.scheduler.clock.ControlledActorClock;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.example.camunda.Constants;
import org.example.camunda.core.PayloadGenerator;
import org.example.camunda.dto.*;
import org.example.camunda.utils.*;

public class ZeebeTestUtils {

  private static InMemoryEngine engine = null;
  private static ControlledActorClock clock = null;
  private static ZeebeClient client = null;
  private static SortedMap<Long, List<TestAction>> timedActions = null;
  private static Map<String, List<TestAction>> records = null;
  private static ExecutionPlan plan = null;
  private static List<JobWorker> workers = null;

  private static Set<Long> handled = null;
  private static long timeMillis;

  private static final Map<Long, String> processScenarioMap = new HashMap<>();

  public static Map<String, List<TestAction>> test(ExecutionPlan tstplan)
      throws NoSuchFieldException, IllegalAccessException {
    plan = tstplan;
    handled = new HashSet<>();
    processScenarioMap.clear();
    engine = (InMemoryEngine) EngineFactory.create();
    engine.start();
    clock = ReflectionUtils.getClock(engine);
    timedActions = new TreeMap<>();
    records = new HashMap<>();
    client = engine.createClient();
    workers = new ArrayList<>();
    BpmnSimulatorModifUtils.prepareSimulation(plan);
    deploy(plan);
    prepareWorkers(plan);
    startInstances(plan);
    logWork();
    while (timeMillis > System.currentTimeMillis() - 1000 || !timedActions.isEmpty()) {
      nextActions();
      ThreadUtils.pause(300);
    }
    engine
        .getRecordStreamSource()
        .getRecords()
        .forEach(
            record -> {
              if (record.getValueType() == ValueType.PROCESS_INSTANCE
                  && record.getRecordType() == RecordType.EVENT
                  && record.getIntent() == ProcessInstanceIntent.ELEMENT_COMPLETING) {
                ProcessInstanceRecord instance = (ProcessInstanceRecord) record.getValue();
                String scenario = processScenarioMap.get(instance.getProcessInstanceKey());
                if (scenario != null) {
                  TestAction a = new TestAction();
                  a.setElementId(instance.getElementId());
                  a.setExpected(true);
                  a.setType(StepActionEnum.COMPLETE);
                  a.setMessage("element completed");
                  addRecord(scenario, a);
                }
              } else if (record.getValueType() == ValueType.VARIABLE
                  && record.getIntent() == VariableIntent.CREATED) {
                VariableRecord var = (VariableRecord) record.getValue();
                if (var.getName().equals(Constants.UNIQUE_ID_KEY)) {
                  String scenario = JsonUtils.toObject(var.getValue(), String.class);
                  processScenarioMap.put(var.getProcessInstanceKey(), scenario);
                }
              } else if (record.getValueType() == ValueType.INCIDENT) {
                IncidentRecord incident = (IncidentRecord) record.getValue();
                String scenario = processScenarioMap.get(incident.getProcessInstanceKey());
                TestAction a = new TestAction();
                a.setJobKey(incident.getJobKey());
                a.setElementId(incident.getElementId());
                a.setExpected(false);
                a.setErrorCode("Engine incident");
                a.setMessage(incident.getErrorMessage());
                addRecord(scenario, a);
              }
            });
    for (JobWorker w : workers) {
      w.close();
    }
    client.close();
    engine.stop();
    return records;
  }

  private static void logWork() {
    timeMillis = System.currentTimeMillis();
  }

  private static synchronized void addAction(long time, TestAction action) {
    if (!timedActions.containsKey(time)) {
      timedActions.put(time, new ArrayList<>());
    }
    if (action != null) {
      timedActions.get(time).add(action);
    }
  }

  private static void startInstances(ExecutionPlan plan) {

    for (Scenario s : plan.getScenarii()) {
      s.setBpmnProcessId(plan.getDefinition().getBpmnProcessId());
      s.setVersion(plan.getDefinition().getVersion());
      startInstance(s);
    }
  }

  private static void startInstance(Scenario s) {
    ObjectNode variables = getVariables(s.getJsonTemplate().getTemplate(), null);

    ContextUtils.addInstance(s.getName(), s, 1.0);
    variables.put(Constants.UNIQUE_ID_KEY, s.getName());

    if (InstanceStartTypeEnum.MSG == s.getStartType()) {
      message(s.getStartMsgName(), s.getName(), variables);
      TestAction a =
          TestActionBuilder.message(s.getName(), null, s.getStartMsgName(), s.getName(), variables);
      a.setTime(clock.getCurrentTime().toEpochMilli());
      addRecord(s.getName(), a);
    } else {
      startProcessInstance(s.getBpmnProcessId(), variables);
      addRecord(
          s.getName(),
          TestActionBuilder.start(s.getName(), variables, clock.getCurrentTime().toEpochMilli()));
    }
  }

  public static void addRecord(String scenario, TestAction action) {
    if (!records.containsKey(scenario)) {
      records.put(scenario, new ArrayList<>());
    }
    records.get(scenario).add(action);
  }

  public static void startProcessInstance(String bpmnProcessId, Object variables) {
    client
        .newCreateInstanceCommand()
        .bpmnProcessId(bpmnProcessId)
        .latestVersion()
        .variables(variables)
        .send()
        .join();
  }

  public static void message(String message, String correlationKey, JsonNode variables) {
    logWork();
    client
        .newPublishMessageCommand()
        .messageName(message)
        .correlationKey(correlationKey)
        .variables(variables)
        .send()
        .join();
  }

  public static void incident(TestAction a) {
    logWork();
    client
        .newFailCommand(a.getJobKey())
        .retries(0)
        .errorMessage(IncidentUtils.error(a.getIncident()))
        .send()
        .join();
  }

  public static synchronized void complete(TestAction a) {
    logWork();
    try {
      client.newCompleteCommand(a.getJobKey()).variables(a.getVariables()).send().join();
    } catch (Exception e) {

    }
  }

  public static void signal(TestAction a) {
    logWork();
    client
        .newBroadcastSignalCommand()
        .signalName(a.getSignal())
        .variables(a.getVariables())
        .send()
        .join();
  }

  public static void bpmnError(TestAction a) {
    logWork();
    client
        .newThrowErrorCommand(a.getJobKey())
        .errorCode(a.getErrorCode())
        .variables(a.getVariables())
        .send()
        .join();
  }

  private static ObjectNode getVariables(String payloadTemplate, Map<String, Object> variables) {
    return JsonUtils.toJsonNode(PayloadGenerator.generatePayload(payloadTemplate, variables));
  }

  private static void deploy(String name, String bpmnXml) {
    client
        .newDeployResourceCommand()
        .addResourceString(bpmnXml, StandardCharsets.UTF_8, name)
        .send()
        .join();
  }

  private static void deploy(ExecutionPlan plan) {
    // if (plan.getXmlModified() || plan.getDefinition().getVersion() < 0) {
    deploy(plan.getDefinition().getName() + ".bpmn", plan.getXml());
    if (plan.getXmlDependencies() != null) {
      for (String dep : plan.getXmlDependencies().keySet()) {
        deploy(dep + ".bpmn", plan.getXmlDependencies().get(dep));
      }
    }
    if (plan.getDmnDependencies() != null) {
      for (String dep : plan.getDmnDependencies().keySet()) {
        deploy(dep + ".dmn", plan.getDmnDependencies().get(dep));
      }
    }
  }

  private static synchronized boolean shouldProceed(long jobKey) {
    if (handled.contains(jobKey)) {
      return false;
    }
    handled.add(jobKey);
    return true;
  }

  private static void prepareWorkers(ExecutionPlan plan) {
    List<String> jobTypes = BpmnUtils.getJobTypes(plan.getXml());
    if (plan.getXmlDependencies() != null) {
      for (String xml : plan.getXmlDependencies().values()) {
        jobTypes.addAll(BpmnUtils.getJobTypes(xml));
      }
    }
    workers.add(
        client
            .newWorker()
            .jobType("processTerminated")
            .handler(
                new JobHandler() {
                  @Override
                  public void handle(JobClient client, ActivatedJob job) throws Exception {
                    // Map<String, Object> variables = job.getVariablesAsMap();
                    // String processUniqueId = (String) variables.get(Constants.UNIQUE_ID_KEY);
                    // ContextUtils.instanceCompleted(processUniqueId);
                    client.newCompleteCommand(job.getKey()).send();
                  }
                })
            .open());
    workers.add(
        client
            .newWorker()
            .jobType("startEventListener")
            .handler(
                new JobHandler() {
                  @Override
                  public void handle(JobClient client, ActivatedJob job) throws Exception {
                    // Map<String, Object> variables = job.getVariablesAsMap();
                    // String processUniqueId = (String) variables.get(Constants.UNIQUE_ID_KEY);
                    // ContextUtils.addInstanceKey(processUniqueId, job.getProcessInstanceKey());
                    client.newCompleteCommand(job.getKey()).send();
                  }
                })
            .open());
    for (String jobType : jobTypes) {
      workers.add(
          client
              .newWorker()
              .jobType(jobType)
              .handler(
                  new JobHandler() {
                    @Override
                    public void handle(JobClient client, ActivatedJob job) throws Exception {
                      if (!shouldProceed(job.getKey())) {
                        return;
                      }
                      logWork();
                      Map<String, Object> variables = job.getVariablesAsMap();
                      String processUniqueId = (String) variables.get(Constants.UNIQUE_ID_KEY);
                      InstanceContext context = ContextUtils.getContext(processUniqueId);

                      StepExecPlan step = context.getScenario().getSteps().get(job.getElementId());
                      if (step == null) {
                        TestAction a = new TestAction();
                        a.setJobKey(job.getKey());
                        a.setElementId(job.getElementId());
                        a.setExpected(false);
                        a.setTime(clock.getCurrentTime().toEpochMilli());
                        a.setIncomingVariables(variables);
                        addRecord(context.getScenario().getName(), a);
                        client
                            .newFailCommand(job.getKey())
                            .retries(0)
                            .errorMessage("Error, step is not defined")
                            .send();
                        return;
                      }
                      if (step.getPreSteps() != null) {
                        for (StepAdditionalAction preStep : step.getPreSteps()) {
                          addAdditionalStep(
                              clock.getCurrentTime().toEpochMilli(), job, preStep, context);
                        }
                      }
                      // main step action
                      if (step.getAction() == StepActionEnum.DO_NOTHING) {
                        TestAction a = new TestAction();
                        a.setJobKey(job.getKey());
                        a.setElementId(job.getElementId());
                        a.setExpected(true);
                        a.setTime(clock.getCurrentTime().toEpochMilli());
                        a.setType(StepActionEnum.DO_NOTHING);
                        a.setIncomingVariables(variables);
                        addRecord(context.getScenario().getName(), a);
                      }
                      if (step.getAction() == StepActionEnum.INCIDENT) {
                        long targetTime =
                            clock.getCurrentTime().toEpochMilli()
                                + ScenarioUtils.durationToMillis(
                                    plan, step.getDuration().getStartDesiredAvg());
                        addAction(
                            targetTime,
                            TestActionBuilder.incident(
                                context.getScenario().getName(),
                                job.getElementId(),
                                job.getKey(),
                                step.getIncident(),
                                variables));
                      } else if (step.getAction() == StepActionEnum.COMPLETE) {
                        long targetTime =
                            clock.getCurrentTime().toEpochMilli()
                                + ScenarioUtils.durationToMillis(
                                    plan, step.getDuration().getStartDesiredAvg());

                        addAction(
                            targetTime,
                            TestActionBuilder.complete(
                                context.getScenario().getName(),
                                job.getElementId(),
                                job.getKey(),
                                getVariables(step.getJsonTemplate().getTemplate(), variables),
                                variables));
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
                  })
              .open());
    }
  }

  public static void nextActions() {
    if (!timedActions.isEmpty()) {
      long timedKey = timedActions.firstKey();
      setClock(timedKey);
      List<TestAction> actions = timedActions.get(timedKey);
      while (actions != null && !actions.isEmpty()) {
        for (TestAction a : actions) {
          if (StepActionEnum.MSG.equals(a.getType())) {
            message(a.getMessage(), a.getCorrelationKey(), a.getVariables());
          } else if (StepActionEnum.INCIDENT.equals(a.getType())) {
            incident(a);
          } else if (StepActionEnum.COMPLETE.equals(a.getType())) {
            complete(a);
          } else if (StepActionEnum.SIGNAL.equals(a.getType())) {
            signal(a);
          } else if (StepActionEnum.BPMN_ERROR.equals(a.getType())) {
            bpmnError(a);
          }
          a.setExpected(true);
          a.setTime(clock.getCurrentTime().toEpochMilli());
          addRecord(a.getScenario(), a);
        }
        timedActions.get(timedKey).removeAll(actions);
        actions = timedActions.get(timedKey);
      }
      timedActions.remove(timedKey);
    }
  }

  public static synchronized void setClock(Long time) {
    // long duration = clock - engine.clock.getCurrentTime().toEpochMilli();
    logWork();
    clock.setCurrentTime(time);
  }

  public static void addAdditionalStep(
      Long baseDate, ActivatedJob job, StepAdditionalAction step, InstanceContext context) {
    Map<String, Object> variables = job.getVariablesAsMap();
    long dateTarget = baseDate + ScenarioUtils.durationToMillis(plan, step.getDelay());
    if (step.getType() == StepActionEnum.CLOCK) {
      addAction(dateTarget, null);
    } else if (step.getType() == StepActionEnum.MSG) {
      addAction(
          dateTarget,
          TestActionBuilder.message(
              context.getScenario().getName(),
              job.getElementId(),
              step.getMsg(),
              ScenarioUtils.getCorrelationKeyValue(variables, step.getCorrelationKey()),
              getVariables(step.getJsonTemplate().getTemplate(), variables)));
    } else if (step.getType() == StepActionEnum.SIGNAL) {
      addAction(
          dateTarget,
          TestActionBuilder.signal(
              context.getScenario().getName(),
              job.getElementId(),
              step.getSignal(),
              getVariables(step.getJsonTemplate().getTemplate(), variables)));
    } else if (step.getType() == StepActionEnum.BPMN_ERROR) {
      addAction(
          dateTarget,
          TestActionBuilder.bpmnError(
              context.getScenario().getName(),
              job.getElementId(),
              job.getKey(),
              step.getErrorCode(),
              getVariables(step.getJsonTemplate().getTemplate(), variables)));
    }
  }
}
