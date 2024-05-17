package org.example.camunda.dto;

import java.util.List;

public class StepExecPlan {
  private String elementId;
  private StepActionEnum action = StepActionEnum.COMPLETE;
  // COMPLETE
  private StepDuration duration;

  // MSG or COMPLETE
  private String jsonTemplate = "{}";
  private List<StepAdditionalAction> postSteps;
  private List<StepAdditionalAction> preSteps;

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

  public List<StepAdditionalAction> getPostSteps() {
    return postSteps;
  }

  public void setPostSteps(List<StepAdditionalAction> postSteps) {
    this.postSteps = postSteps;
  }

  public List<StepAdditionalAction> getPreSteps() {
    return preSteps;
  }

  public void setPreSteps(List<StepAdditionalAction> preSteps) {
    this.preSteps = preSteps;
  }
}
