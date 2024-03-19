package org.example.camunda.core;

import com.fasterxml.jackson.databind.JsonNode;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ClientException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ZeebeService {

  @Autowired private ZeebeClient zeebeClient;
  @Autowired private RestTemplate restTemplate;

  @Value("${gateway.actuator:http://zeebe:9600/actuator}")
  private String gatewayActuator;

  private Runnable engineIdleCallback = null;
  private boolean engineIdle = true;
  Timer engineIdleTimer = null;

  private synchronized void schedule(TimerTask task) {
    if (engineIdleTimer != null) {
      engineIdleTimer.cancel();
    }
    engineIdleTimer = new Timer();
    engineIdleTimer.schedule(task, 600);
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

  public Long startProcessInstance(String bpmnProcessId, int version, Object variables) {
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

      try {
        Thread.sleep(100);
      } catch (InterruptedException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      return startProcessInstance(bpmnProcessId, version, variables);
    }
  }

  public Long setClock(long time) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>("{\"epochMilli\": " + time + "}", headers);
    Map<String, Object> result =
        restTemplate.postForObject(gatewayActuator + "/clock/pin", request, Map.class);
    return (long) result.get("epochMilli");
  }

  public Long moveClock(long time) {
    zeebeWorks();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>("{\"offsetMilli\": " + time + "}", headers);
    Map result = restTemplate.postForObject(gatewayActuator + "/clock/add", request, Map.class);
    return (long) result.get("epochMilli");
  }

  public void completeJob(Long jobKey, JsonNode variables) {
    zeebeWorks();
    try {
      this.zeebeClient.newCompleteCommand(jobKey).variables(variables).send().join();
    } catch (ClientException e) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      completeJob(jobKey, variables);
    }
  }
}
