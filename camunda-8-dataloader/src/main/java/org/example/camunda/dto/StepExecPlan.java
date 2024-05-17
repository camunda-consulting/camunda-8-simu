package org.example.camunda.dto;

public class StepExecPlan {
  private String elementId;
  private StepActionEnum action = StepActionEnum.COMPLETE;
  // COMPLETE
  private StepDuration duration;

  // MSG or COMPLETE
  private String jsonTemplate = "{}";
  private PostStepAction postStep;

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

  public String getJsonTemplate() {
    return jsonTemplate;
  }

  public void setJsonTemplate(String jsonTemplate) {
    this.jsonTemplate = jsonTemplate;
  }

  public PostStepAction getPostStep() {
    return postStep;
  }

  public void setPostStep(PostStepAction postStep) {
    this.postStep = postStep;
  }
}
