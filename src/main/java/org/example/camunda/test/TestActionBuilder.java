package org.example.camunda.test;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.example.camunda.dto.IncidentTypeEnum;
import org.example.camunda.dto.StepActionEnum;

public class TestActionBuilder {

  public static TestAction start(String scenario, JsonNode variables, long engineTime) {
    TestAction a = new TestAction();
    a.setScenario(scenario);
    a.setType(StepActionEnum.START);
    a.setVariables(variables);
    a.setTime(engineTime);
    return a;
  }

  public static TestAction bpmnError(
      String scenario, String elementId, long jobKey, String errorCode, JsonNode variables) {
    TestAction a = new TestAction();
    a.setScenario(scenario);
    a.setType(StepActionEnum.BPMN_ERROR);
    a.setElementId(elementId);
    a.setJobKey(jobKey);
    a.setErrorCode(errorCode);
    a.setVariables(variables);
    return a;
  }

  public static TestAction signal(
      String scenario, String elementId, String signal, JsonNode variables) {
    TestAction a = new TestAction();
    a.setScenario(scenario);
    a.setType(StepActionEnum.SIGNAL);
    a.setElementId(elementId);
    a.setSignal(signal);
    a.setVariables(variables);
    return a;
  }

  public static TestAction message(
      String scenario,
      String elementId,
      String message,
      String correlationKey,
      JsonNode variables) {
    TestAction a = new TestAction();
    a.setScenario(scenario);
    a.setType(StepActionEnum.MSG);
    a.setElementId(elementId);
    a.setMessage(message);
    a.setCorrelationKey(correlationKey);
    a.setVariables(variables);
    return a;
  }

  public static TestAction complete(
      String scenario,
      String elementId,
      long jobKey,
      JsonNode variables,
      Map<String, Object> incomingVariables) {
    TestAction a = new TestAction();
    a.setScenario(scenario);
    a.setType(StepActionEnum.COMPLETE);
    a.setElementId(elementId);
    a.setJobKey(jobKey);
    a.setVariables(variables);
    a.setIncomingVariables(incomingVariables);
    return a;
  }

  public static TestAction incident(
      String scenario,
      String elementId,
      long jobKey,
      IncidentTypeEnum incident,
      Map<String, Object> incomingVariables) {
    TestAction a = new TestAction();
    a.setScenario(scenario);
    a.setType(StepActionEnum.INCIDENT);
    a.setElementId(elementId);
    a.setJobKey(jobKey);
    a.setIncident(incident);
    a.setIncomingVariables(incomingVariables);
    return a;
  }
}
