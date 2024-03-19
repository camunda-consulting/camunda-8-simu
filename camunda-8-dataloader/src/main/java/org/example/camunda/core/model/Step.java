package org.example.camunda.core.model;

public class Step {
  private StepType type;
  private String templateVariables;
  private long timeBeforeCompletion;
  private boolean shouldComplete;
  private String elementId;

  public StepType getType() {
    return type;
  }

  public void setType(StepType type) {
    this.type = type;
  }

  public String getTemplateVariables() {
    return templateVariables;
  }

  public void setTemplateVariables(String templateVariables) {
    this.templateVariables = templateVariables;
  }

  public long getTimeBeforeCompletion() {
    return timeBeforeCompletion;
  }

  public void setTimeBeforeCompletion(long timeBeforeCompletion) {
    this.timeBeforeCompletion = timeBeforeCompletion;
  }

  public boolean isShouldComplete() {
    return shouldComplete;
  }

  public void setShouldComplete(boolean shouldComplete) {
    this.shouldComplete = shouldComplete;
  }

  public String getElementId() {
    return elementId;
  }

  public void setElementId(String elementId) {
    this.elementId = elementId;
  }
}
