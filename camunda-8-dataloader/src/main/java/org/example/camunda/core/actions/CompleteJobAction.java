package org.example.camunda.core.actions;

import java.io.IOException;
import java.util.Map;
import org.example.camunda.core.ZeebeService;

public class CompleteJobAction extends Action {

  private Long jobKey;
  private Map<String, Object> variables;
  private String payloadTemplate;

  public CompleteJobAction(
      Long jobKey,
      String payloadTemplate,
      Map<String, Object> variables,
      ZeebeService zeebeService) {
    super();
    this.jobKey = jobKey;
    this.variables = variables;
    this.payloadTemplate = payloadTemplate;
    this.setZeebeService(zeebeService);
  }

  @Override
  public void run() {
    try {
      this.getZeebeService()
          .completeJob(this.jobKey, getVariables(this.payloadTemplate, variables));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
