package org.example.camunda.core.actions;

import java.util.Map;
import org.example.camunda.Constants;
import org.example.camunda.core.ZeebeService;
import org.example.camunda.utils.ContextUtils;

public class BpmnErrorAction extends Action {

  private final String error;
  private final Long jobKey;
  private final Map<String, Object> variables;
  private final String payloadTemplate;
  private final long time;

  public BpmnErrorAction(
      String error,
      Long jobKey,
      String payloadTemplate,
      Map<String, Object> variables,
      ZeebeService zeebeService,
      long time) {
    super();
    this.error = error;
    this.jobKey = jobKey;
    this.variables = variables;
    this.payloadTemplate = payloadTemplate;
    this.setZeebeService(zeebeService);
    this.time = time;
  }

  @Override
  public void run() {
    ContextUtils.setProcessInstanceTime((String) variables.get(Constants.UNIQUE_ID_KEY), time);
    this.getZeebeService().bpmnError(error, jobKey, getVariables(this.payloadTemplate, variables));
  }

  @Override
  public String getType() {
    return "BpmnError";
  }
}
