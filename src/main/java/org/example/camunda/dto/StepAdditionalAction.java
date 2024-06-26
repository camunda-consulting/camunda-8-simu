package org.example.camunda.dto;

import org.example.camunda.dto.templating.JsonTemplate;

public class StepAdditionalAction {
  private StepActionEnum type = StepActionEnum.CLOCK;

  // CLOCK
  private String feelDelay = "PT5M";

  // MSG
  private String signal = "Signal";
  private String msg = "MSG";
  private Long msgDelay = 1000L;
  private String correlationKey;
  private JsonTemplate jsonTemplate;

  // BPMN_ERROR
  private String errorCode;
  private Long errorDelay = 1000L;

  public StepActionEnum getType() {
    return type;
  }

  public void setType(StepActionEnum type) {
    this.type = type;
  }

  public String getFeelDelay() {
    return feelDelay;
  }

  public void setFeelDelay(String feelDelay) {
    this.feelDelay = feelDelay;
  }

  public Long getMsgDelay() {
    return msgDelay;
  }

  public void setMsgDelay(Long msgDelay) {
    this.msgDelay = msgDelay;
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

  public Long getErrorDelay() {
    return errorDelay;
  }

  public void setErrorDelay(Long errorDelay) {
    this.errorDelay = errorDelay;
  }
}
