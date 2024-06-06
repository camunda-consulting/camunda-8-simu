package org.example.camunda.dto;

import io.camunda.operate.model.FlowNodeInstance;
import io.camunda.operate.model.ProcessInstance;
import java.util.List;
import java.util.Map;

public class ProcessInstanceMigration {
  private Long originalProcessInstanceKey;
  private ProcessInstance originalProcessInstance;
  private List<FlowNodeInstance> activeFlowNodes;
  private Map<String, Object> variables;
  private Long newProcessInstanceKey;

  public Long getOriginalProcessInstanceKey() {
    return originalProcessInstanceKey;
  }

  public ProcessInstanceMigration setOriginalProcessInstanceKey(Long originalProcessInstanceKey) {
    this.originalProcessInstanceKey = originalProcessInstanceKey;
    return this;
  }

  public ProcessInstance getOriginalProcessInstance() {
    return originalProcessInstance;
  }

  public ProcessInstanceMigration setOriginalProcessInstance(
      ProcessInstance originalProcessInstance) {
    this.originalProcessInstance = originalProcessInstance;
    return this;
  }

  public List<FlowNodeInstance> getActiveFlowNodes() {
    return activeFlowNodes;
  }

  public ProcessInstanceMigration setActiveFlowNodes(List<FlowNodeInstance> activeFlowNodes) {
    this.activeFlowNodes = activeFlowNodes;
    return this;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  public ProcessInstanceMigration setVariables(Map<String, Object> variables) {
    this.variables = variables;
    return this;
  }

  public Long getNewProcessInstanceKey() {
    return newProcessInstanceKey;
  }

  public ProcessInstanceMigration setNewProcessInstanceKey(Long newProcessInstanceKey) {
    this.newProcessInstanceKey = newProcessInstanceKey;
    return this;
  }
}
