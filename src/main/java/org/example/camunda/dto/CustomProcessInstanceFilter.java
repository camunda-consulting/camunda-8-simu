package org.example.camunda.dto;

import io.camunda.operate.search.ProcessInstanceFilter;

public class CustomProcessInstanceFilter extends ProcessInstanceFilter {
  private Long parentFlowNodeInstanceKey;

  public CustomProcessInstanceFilter(Long parentKey, Long parentFlowNodeInstanceKey) {
    super();
    this.setParentKey(parentKey);
    this.parentFlowNodeInstanceKey = parentFlowNodeInstanceKey;
  }

  public Long getParentFlowNodeInstanceKey() {
    return parentFlowNodeInstanceKey;
  }

  public void setParentFlowNodeInstanceKey(Long parentFlowNodeInstanceKey) {
    this.parentFlowNodeInstanceKey = parentFlowNodeInstanceKey;
  }
}
