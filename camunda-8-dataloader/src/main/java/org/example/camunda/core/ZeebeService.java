package org.example.camunda.core;

import com.fasterxml.jackson.databind.JsonNode;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ClientException;
import io.camunda.zeebe.client.api.command.ClientStatusException;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.grpc.Status;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.example.camunda.service.ScenarioExecService;
import org.example.camunda.utils.ContextUtils;
import org.example.camunda.utils.FeelUtils;
import org.example.camunda.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ZeebeService {
  private static final Logger LOG = LoggerFactory.getLogger(ScenarioExecService.class);

  @Autowired private ZeebeClient zeebeClient;
  @Autowired private RestTemplate restTemplate;

  @Value("${gateway.actuator:http://zeebe:9600/actuator}")
  private String gatewayActuator;

  private Runnable engineIdleCallback = null;
  private boolean engineIdle = true;
  Timer engineIdleTimer = null;

  public DeploymentEvent deploy(String name, String bpmnXml) {
    return zeebeClient
        .newDeployResourceCommand()
        .addResourceString(bpmnXml, StandardCharsets.UTF_8, name)
        .send()
        .join();
  }

  private synchronized void schedule(TimerTask task) {
    if (engineIdleTimer != null) {
      engineIdleTimer.cancel();
    }
    engineIdleTimer = new Timer();
    engineIdleTimer.schedule(task, ContextUtils.getIdleTimeBeforeClockMove());
  }

  public void zeebeWorks() {
    if (engineIdleCallback != null) {
      engineIdle = false;
      schedule(
          new TimerTask() {

            @Override
            public void run() {
              engineIdle = true;
              if (engineIdleCallback != null) {
                engineIdleCallback.run();
              }
            }
          });
    }
  }

  public void waitEngineToBeIdle(Runnable callback) {
    this.engineIdleCallback = callback;
    if (this.engineIdle) {
      this.engineIdleCallback.run();
    }
  }

  public Long startProcessInstance(String bpmnProcessId, Long version, Object variables) {
    zeebeWorks();
    try {
      return zeebeClient
          .newCreateInstanceCommand()
          .bpmnProcessId(bpmnProcessId)
          .latestVersion()
          .variables(variables)
          .send()
          .join()
          .getProcessInstanceKey();
    } catch (ClientException e) {
      ThreadUtils.pause(200);
      return startProcessInstance(bpmnProcessId, version, variables);
    }
  }

  public Long setClock(long time) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>("{\"epochMilli\": " + time + "}", headers);
    Map<String, Object> result =
        restTemplate.postForObject(gatewayActuator + "/clock/pin", request, Map.class);
    FeelUtils.setClock(time);
    zeebeWorks();
    return (long) result.get("epochMilli");
  }

  public void message(String message, String correlationKey, JsonNode variables) {
    zeebeWorks();
    try {
      this.zeebeClient
          .newPublishMessageCommand()
          .messageName(message)
          .correlationKey(correlationKey)
          .variables(variables)
          .send()
          .join();
    } catch (ClientException e) {
      if (e instanceof ClientStatusException
              && (((ClientStatusException) e).getStatus().getCode() == Status.Code.NOT_FOUND)
          || (((ClientStatusException) e).getStatus().getCode() == Status.Code.CANCELLED)) {
        LOG.error(
            "Error publishing message " + message + "with correlation key " + correlationKey, e);
      } else {
        ThreadUtils.pause(200);
        message(message, correlationKey, variables);
      }
    }
  }

  public void completeJob(Long jobKey, JsonNode variables) {
    zeebeWorks();
    try {
      this.zeebeClient.newCompleteCommand(jobKey).variables(variables).send().join();
    } catch (ClientException e) {
      if (e instanceof ClientStatusException
              && (((ClientStatusException) e).getStatus().getCode() == Status.Code.NOT_FOUND)
          || (((ClientStatusException) e).getStatus().getCode() == Status.Code.CANCELLED)) {
        LOG.error("Error completing " + jobKey, e);
      } else {
        ThreadUtils.pause(200);
        completeJob(jobKey, variables);
      }
    }
  }

  public JobWorker createStreamingWorker(String jobType, JobHandler jobHandler) {
    return this.zeebeClient
        .newWorker()
        .jobType(jobType)
        .handler(jobHandler)
        .name(jobType)
        .maxJobsActive(100)
        .timeout(2000)
        // .streamTimeout()
        .streamEnabled(true)
        .open();
  }
}
