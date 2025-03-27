package org.example.camunda.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.example.camunda.dto.IncidentTypeEnum;
import org.example.camunda.dto.StepActionEnum;

@JsonInclude(Include.NON_NULL)
public class TestAction {
  private StepActionEnum type;
  private String elementId;
  private Long jobKey;
  private String message;
  private String errorCode;
  private String signal;
  private IncidentTypeEnum incident;
  private String correlationKey;
  private JsonNode variables;
  private long time;
  private boolean expected;
  private String scenario;
  private Map<String, Object> incomingVariables;

  public StepActionEnum getType() {
    return type;
  }

  public void setType(StepActionEnum type) {
    this.type = type;
  }

  public String getElementId() {
    return elementId;
  }

  public void setElementId(String elementId) {
    this.elementId = elementId;
  }

  public Long getJobKey() {
    return jobKey;
  }

  public void setJobKey(Long jobKey) {
    this.jobKey = jobKey;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public String getSignal() {
    return signal;
  }

  public void setSignal(String signal) {
    this.signal = signal;
  }

  public String getCorrelationKey() {
    return correlationKey;
  }

  public void setCorrelationKey(String correlationKey) {
    this.correlationKey = correlationKey;
  }

  public JsonNode getVariables() {
    return variables;
  }

  public void setVariables(JsonNode variables) {
    this.variables = variables;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public IncidentTypeEnum getIncident() {
    return incident;
  }

  public void setIncident(IncidentTypeEnum incident) {
    this.incident = incident;
  }

  public boolean isExpected() {
    return expected;
  }

  public void setExpected(boolean expected) {
    this.expected = expected;
  }

  public String getScenario() {
    return scenario;
  }

  public void setScenario(String scenario) {
    this.scenario = scenario;
  }

  public Map<String, Object> getIncomingVariables() {
    return incomingVariables;
  }

  public void setIncomingVariables(Map<String, Object> incomingVariables) {
    this.incomingVariables = incomingVariables;
  }
}
