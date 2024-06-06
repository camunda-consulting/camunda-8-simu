package org.example.camunda.core.actions;

import java.util.Map;
import org.example.camunda.core.ZeebeService;

public class MessageAction extends Action {

  private String message;
  private String correlationKey;
  private Map<String, Object> variables;
  private String payloadTemplate;

  public MessageAction(
      String message,
      String correlationKey,
      String payloadTemplate,
      Map<String, Object> variables,
      ZeebeService zeebeService) {
    super();
    this.message = message;
    this.correlationKey = correlationKey;
    this.variables = variables;
    this.payloadTemplate = payloadTemplate;
    this.setZeebeService(zeebeService);
  }

  @Override
  public void run() {
    this.getZeebeService()
        .message(message, correlationKey, getVariables(this.payloadTemplate, variables));
  }
}
