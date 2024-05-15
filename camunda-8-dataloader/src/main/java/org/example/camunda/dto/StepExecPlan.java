package org.example.camunda.dto;

public class StepExecPlan {
  private String elementId;
  private StepActionEnum action = StepActionEnum.COMPLETE;
  // COMPLETE
  private StepDuration duration;
  // CLOCK
  private Long timeAdvance = 1000L;
  // MSG
  private String msg = "MSG";
  private String correlationId;
  private Long delayBeforeMsg = 400L;

  // MSG or COMPLETE
  private String jsonTemplate = "{}";

  public String getElementId() {
    return elementId;
  }

  public void setElementId(String elementId) {
    this.elementId = elementId;
  }

  public StepActionEnum getAction() {
    return action;
  }

  public void setAction(StepActionEnum action) {
    this.action = action;
  }

  public StepDuration getDuration() {
    return duration;
  }

  public void setDuration(StepDuration duration) {
    this.duration = duration;
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
