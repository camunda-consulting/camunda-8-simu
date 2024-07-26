package org.example.camunda.dto;

import org.example.camunda.dto.templating.JsonTemplate;

public class StepAdditionalAction {
  private StepActionEnum type = StepActionEnum.CLOCK;
  private String delay = "PT5M";

  // MSG
  private String signal = "Signal";
  private String msg = "MSG";
  private String correlationKey;
  private JsonTemplate jsonTemplate;

  // BPMN_ERROR
  private String errorCode;

  public StepActionEnum getType() {
    return type;
  }

  public void setType(StepActionEnum type) {
    this.type = type;
  }

  public String getDelay() {
    return delay;
  }

  public void setFeelDelay(String delay) {
    this.delay = delay;
  }

  public String getSignal() {
    return signal;
  }

  public void setSignal(String signal) {
    this.signal = signal;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public String getCorrelationKey() {
    return correlationKey;
  }

  public void setCorrelationKey(String correlationKey) {
    this.correlationKey = correlationKey;
  }

  public JsonTemplate getJsonTemplate() {
    return jsonTemplate;
  }

  public void setJsonTemplate(JsonTemplate jsonTemplate) {
    this.jsonTemplate = jsonTemplate;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }
}
