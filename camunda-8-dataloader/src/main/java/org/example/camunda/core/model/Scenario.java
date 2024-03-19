package org.example.camunda.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scenario {
  private Long processInstanceKey;
  private String bpmnProcessId;
  private int version = -1;
  private List<Step> steps = new ArrayList<>();
  private Map<String, Step> elementIdStepMap = new HashMap<>();

  public Long getProcessInstanceKey() {
    return processInstanceKey;
  }

  public void setProcessInstanceKey(Long processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
  }

  public String getBpmnProcessId() {
    return bpmnProcessId;
  }

  public void setBpmnProcessId(String bpmnProcessId) {
    this.bpmnProcessId = bpmnProcessId;
  }

  public List<Step> getSteps() {
    return steps;
  }

  public void setSteps(List<Step> steps) {
    this.steps = steps;
  }

  public Map<String, Step> getElementIdStepMap() {
    return elementIdStepMap;
  }

  public void setElementIdStepMap(Map<String, Step> elementIdStepMap) {
    this.elementIdStepMap = elementIdStepMap;
  }

  public void addStep(Step step) {
    steps.add(step);
    elementIdStepMap.put(step.getElementId(), step);
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getStartPayload() {
    return steps.get(0).getTemplateVariables();
  }

  public Step getStep(String elementId) {
    // TODO Auto-generated method stub
    return elementIdStepMap.get(elementId);
  }
}
