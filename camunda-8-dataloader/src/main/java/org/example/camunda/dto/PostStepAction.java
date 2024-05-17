package org.example.camunda.dto;

public class PostStepAction {
  private StepActionEnum type = StepActionEnum.CLOCK;

  // CLOCK
  private Long timeAdvance = 1000L;

  // MSG
  private String msg = "MSG";
  private String correlationId;
  private Long delayBeforeMsg = 0L;
  private String jsonTemplate = "{}";

  public StepActionEnum getType() {
    return type;
  }

  public void setType(StepActionEnum type) {
    this.type = type;
  }

  public Long getTimeAdvance() {
    return timeAdvance;
  }

  public void setTimeAdvance(Long timeAdvance) {
    this.timeAdvance = timeAdvance;
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

  public Long getDelayBeforeMsg() {
    return delayBeforeMsg;
  }

  public void setDelayBeforeMsg(Long delayBeforeMsg) {
    this.delayBeforeMsg = delayBeforeMsg;
  }

  public String getJsonTemplate() {
    return jsonTemplate;
  }

  public void setJsonTemplate(String jsonTemplate) {
    this.jsonTemplate = jsonTemplate;
  }
}
