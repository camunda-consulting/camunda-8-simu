package org.example.camunda.dto;

public class StepAdditionalAction {
  private StepActionEnum type = StepActionEnum.CLOCK;

  // CLOCK AND MSG
  private Long delay = 1000L;

  // MSG
  private String msg = "MSG";
  private String correlationId;
  private String jsonTemplate = "{}";

  public StepActionEnum getType() {
    return type;
  }

  public void setType(StepActionEnum type) {
    this.type = type;
  }

  public Long getDelay() {
    return delay;
  }

  public void setDelay(Long delay) {
    this.delay = delay;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
  }

  public String getJsonTemplate() {
    return jsonTemplate;
  }

  public void setJsonTemplate(String jsonTemplate) {
    this.jsonTemplate = jsonTemplate;
  }
}
