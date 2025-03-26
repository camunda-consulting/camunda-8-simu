package org.example.camunda.core;

import com.fasterxml.jackson.databind.JsonNode;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ClientException;
import io.camunda.zeebe.client.api.command.ClientStatusException;
import io.camunda.zeebe.client.api.command.DeployResourceCommandStep1;
import io.camunda.zeebe.client.api.command.DeployResourceCommandStep1.DeployResourceCommandStep2;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.grpc.Status;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.example.camunda.conf.ClientConfiguration;
import org.example.camunda.dto.IncidentTypeEnum;
import org.example.camunda.service.ScenarioExecService;
import org.example.camunda.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

@Service
public class ZeebeService {

  private static final Logger LOG = LoggerFactory.getLogger(ScenarioExecService.class);
  private static final int RETRY_BASE = 200;
  private ZeebeClient zeebeClient = null;

  @Autowired private ClientConfiguration clientConf;

  public ZeebeClient zeebeClient() {
    return zeebeClient(false);
  }

  public ZeebeClient zeebeClient(boolean force) {
    if (zeebeClient == null || force) {
      if (zeebeClient != null) {
        zeebeClient.close();
      }
      try {
        zeebeClient = clientConf.getZeebeClient();

        zeebeClient.newTopologyRequest().send().join();
      } catch (ClientException e) {
        HistoUtils.addError(e);
        return zeebeClient(force);
      }
    }
    return zeebeClient;
  }

  public DeploymentEvent deploy(String name, String bpmnXml) {
    return zeebeClient()
        .newDeployResourceCommand()
        .addResourceString(bpmnXml, StandardCharsets.UTF_8, name)
        .send()
        .join();
  }

  public void setClock(long time) {
    try {
      zeebeClient().newClockPinCommand().time(time).send().join();
    } catch (CompletionException | ClientException e) {
      zeebeClient(true).newClockPinCommand().time(time).send();
    }
    FeelUtils.setClock(time);
    ContextUtils.setEngineTime(time);
  }

  public void stop() {
    zeebeClient().newClockResetCommand().send().join();
  }

  public void startProcessInstance(String bpmnProcessId, Long version, Object variables) {
    startProcessInstance(bpmnProcessId, version, variables, 1);
  }

  public void startProcessInstance(
      String bpmnProcessId, Long version, Object variables, int retryCount) {
    try {
      zeebeClient()
          .newCreateInstanceCommand()
          .bpmnProcessId(bpmnProcessId)
          .latestVersion()
          .variables(variables)
          .send()
          .join();

    } catch (ClientException e) {
      HistoUtils.addError(e);
      LOG.error(
          "Error starting instance. Retrying after "
              + RETRY_BASE * retryCount
              + ". "
              + e.getMessage());
      ThreadUtils.pause(RETRY_BASE * retryCount);
      startProcessInstance(bpmnProcessId, version, variables, ++retryCount);
    }
  }

  public void message(String message, String correlationKey, JsonNode variables) {
    message(message, correlationKey, variables, 1);
  }

  public void message(String message, String correlationKey, JsonNode variables, int retryCount) {
    try {
      this.zeebeClient()
          .newPublishMessageCommand()
          .messageName(message)
          .correlationKey(correlationKey)
          .variables(variables)
          .timeToLive(Duration.ofDays(1))
          .send()
          .join();
    } catch (ClientException e) {
      if (e instanceof ClientStatusException
              && (((ClientStatusException) e).getStatus().getCode() == Status.Code.NOT_FOUND)
          || (((ClientStatusException) e).getStatus().getCode() == Status.Code.CANCELLED)) {
        LOG.error(
            "Error publishing message " + message + "with correlation key " + correlationKey, e);
      } else {
        HistoUtils.addError(e);
        LOG.error(
            "Error publishing message. Retrying after "
                + RETRY_BASE * retryCount
                + ". "
                + e.getMessage());
        ThreadUtils.pause(RETRY_BASE * retryCount);
        message(message, correlationKey, variables, ++retryCount);
      }
    }
  }

  public void signal(String signal, JsonNode variables) {
    signal(signal, variables, 1);
  }

  public void signal(String signal, JsonNode variables, int retryCount) {

    try {
      this.zeebeClient()
          .newBroadcastSignalCommand()
          .signalName(signal)
          .variables(variables)
          .send()
          .join();
    } catch (ClientException e) {
      if (e instanceof ClientStatusException
              && (((ClientStatusException) e).getStatus().getCode() == Status.Code.NOT_FOUND)
          || (((ClientStatusException) e).getStatus().getCode() == Status.Code.CANCELLED)) {
        LOG.error("Error publishing signal " + signal, e);
      } else {
        HistoUtils.addError(e);
        LOG.error(
            "Error sending signal. Retrying after "
                + RETRY_BASE * retryCount
                + ". "
                + e.getMessage());
        ThreadUtils.pause(RETRY_BASE * retryCount);
        signal(signal, variables, ++retryCount);
      }
    }
  }

  public void bpmnError(String error, Long jobKey, JsonNode variables) {
    bpmnError(error, jobKey, variables, 1);
  }

  public void bpmnError(String error, Long jobKey, JsonNode variables, int retryCount) {
    try {
      this.zeebeClient()
          .newThrowErrorCommand(jobKey)
          .errorCode(error)
          .variables(variables)
          .send()
          .join();
    } catch (ClientException e) {
      if (e instanceof ClientStatusException
              && (((ClientStatusException) e).getStatus().getCode() == Status.Code.NOT_FOUND)
          || (((ClientStatusException) e).getStatus().getCode() == Status.Code.CANCELLED)) {
        LOG.error("Error send BpmnError " + error + "for job " + jobKey, e);
      } else {
        HistoUtils.addError(e);
        LOG.error(
            "Error sending bpmn error. Retrying after "
                + RETRY_BASE * retryCount
                + ". "
                + e.getMessage());
        ThreadUtils.pause(RETRY_BASE * retryCount);
        bpmnError(error, jobKey, variables);
      }
    }
  }

  public void completeJob(Long jobKey, JsonNode variables) {

    completeJob(jobKey, variables, 1);
  }

  public void completeJob(Long jobKey, JsonNode variables, int retryCount) {
    try {
      this.zeebeClient().newCompleteCommand(jobKey).variables(variables).send().join();
    } catch (ClientException e) {
      if (e instanceof ClientStatusException
              && (((ClientStatusException) e).getStatus().getCode() == Status.Code.NOT_FOUND)
          || (((ClientStatusException) e).getStatus().getCode() == Status.Code.CANCELLED)) {
        LOG.error("Error completing " + jobKey, e);
      } else {
        HistoUtils.addError(e);
        LOG.error(
            "Error completing job. Retrying after "
                + RETRY_BASE * retryCount
                + ". "
                + e.getMessage());
        ThreadUtils.pause(RETRY_BASE * retryCount);
        completeJob(jobKey, variables, ++retryCount);
      }
    }
  }

  public void incidentJob(Long jobKey, IncidentTypeEnum incident) {
    incidentJob(jobKey, incident, 1);
  }

  public void incidentJob(Long jobKey, IncidentTypeEnum incident, int retryCount) {
    try {

      this.zeebeClient()
          .newFailCommand(jobKey)
          .retries(0)
          .errorMessage(IncidentUtils.error(incident))
          .send()
          .join();
    } catch (ClientException e) {
      if (e instanceof ClientStatusException
              && (((ClientStatusException) e).getStatus().getCode() == Status.Code.NOT_FOUND)
          || (((ClientStatusException) e).getStatus().getCode() == Status.Code.CANCELLED)) {
        LOG.error("Error completing " + jobKey, e);
      } else {
        HistoUtils.addError(e);
        LOG.error(
            "Error creating incident. Retrying after "
                + RETRY_BASE * retryCount
                + ". "
                + e.getMessage());
        ThreadUtils.pause(RETRY_BASE * retryCount);
        incidentJob(jobKey, incident, ++retryCount);
      }
    }
  }

  public JobWorker createStreamingWorker(String jobType, JobHandler jobHandler) {
    // this.zeebeClient().newProcessInstanceQuery().filter(f ->
    // f.active(true)).send().join()FlownodeInstanceQuery()
    //                  .filter(f -> f.state("Activated")).send()*/
    JobWorker worker =
        this.zeebeClient()
            .newWorker()
            .jobType(jobType)
            .handler(jobHandler)
            .name(jobType)
            .maxJobsActive(30)
            // .timeout(1000)
            // .requestTimeout(Duration.ofMillis(500))
            // .streamTimeout()
            .streamEnabled(true)
            // .streamTimeout(Duration.ofMinutes())

            .open();

    return worker;
  }

  @PostConstruct
  public void autoDeploy() {
    if (clientConf.getZeebe() != null && clientConf.getZeebe().getDeploy() != null) {
      DeployResourceCommandStep1 deployResourceCommand1 = zeebeClient().newDeployResourceCommand();
      DeployResourceCommandStep2 deployResourceCommand = null;
      String resource = clientConf.getZeebe().getDeploy();
      Resource[] array = getResources(resource);
      for (Resource r : array) {
        try (InputStream inputStream = r.getInputStream()) {

          if (deployResourceCommand != null) {
            deployResourceCommand =
                deployResourceCommand.addResourceStream(inputStream, r.getFilename());
          } else {
            deployResourceCommand =
                deployResourceCommand1.addResourceStream(inputStream, r.getFilename());
          }
        } catch (IOException e) {
          throw new RuntimeException(e.getMessage());
        }
      }
      DeploymentEvent deploymentResult = deployResourceCommand.send().join();
      LOG.info(
          "Deployed: {}",
          Stream.concat(
                  deploymentResult.getDecisionRequirements().stream()
                      .map(
                          wf ->
                              String.format(
                                  "<%s:%d>", wf.getDmnDecisionRequirementsId(), wf.getVersion())),
                  deploymentResult.getProcesses().stream()
                      .map(wf -> String.format("<%s:%d>", wf.getBpmnProcessId(), wf.getVersion())))
              .collect(Collectors.joining(",")));
    }
  }

  private Resource[] getResources(String resources) {
    try {
      return new PathMatchingResourcePatternResolver().getResources(resources);
    } catch (IOException e) {
      return new Resource[0];
    }
  }

  public void cancel(long instanceKey) {

    try {
      this.zeebeClient.newCancelInstanceCommand(instanceKey).send().join();
    } catch (ClientException e) {
      HistoUtils.addError(e);
      LOG.error("Error canceling instance " + instanceKey);
    }
  }

  /*
  public void cancelPendingInstances(String name) {

    try {
      SearchQueryResponse
              <ProcessInstance> result = this.zeebeClient().newProcessInstanceQuery().filter(f -> f.active(true)).send().join();

    for(ProcessInstance i : result.items()) {
      if (name.startsWith(i.getBpmnProcessId())) {
        this.zeebeClient.newCancelInstanceCommand(i.getKey()).send();
      }
    }
    } catch(ClientException e) {

    }
    ContextUtils.setNbInstances(0);
  }*/
}
