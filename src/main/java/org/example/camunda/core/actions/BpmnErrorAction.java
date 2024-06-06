package org.example.camunda.core.actions;

import java.util.Map;
import org.example.camunda.core.ZeebeService;

public class BpmnErrorAction extends Action {

  private String error;
  private Long jobKey;
  private Map<String, Object> variables;
  private String payloadTemplate;

  public BpmnErrorAction(
      String error,
      Long jobKey,
      String payloadTemplate,
      Map<String, Object> variables,
      ZeebeService zeebeService) {
    super();
    this.error = error;
    this.jobKey = jobKey;
    this.variables = variables;
    this.payloadTemplate = payloadTemplate;
    this.setZeebeService(zeebeService);
  }

  @Override
  public void run() {
    this.getZeebeService().bpmnError(error, jobKey, getVariables(this.payloadTemplate, variables));
  }
}
