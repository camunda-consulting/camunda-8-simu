package org.example.camunda.dto;

import java.util.List;
import org.example.camunda.dto.templating.JsonTemplate;

public class StepExecPlan {
  private String elementId;
  private StepActionEnum action = StepActionEnum.COMPLETE;

  // COMPLETE AND INCIDENT
  private StepDuration duration;

  // COMPLETE
  private JsonTemplate jsonTemplate;
  private List<StepAdditionalAction> postSteps;

  // ANY
  private List<StepAdditionalAction> preSteps;

  // INCIDENT
  private IncidentTypeEnum incident;

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

  public JsonTemplate getJsonTemplate() {
    return jsonTemplate;
  }

  public void setJsonTemplate(JsonTemplate jsonTemplate) {
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

  public IncidentTypeEnum getIncident() {
    return incident;
  }

  public void setIncident(IncidentTypeEnum incident) {
    this.incident = incident;
  }
}
