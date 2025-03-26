package org.example.camunda.core.actions;

import java.util.Map;
import org.example.camunda.Constants;
import org.example.camunda.core.ZeebeService;
import org.example.camunda.utils.ContextUtils;

public class MessageAction extends Action {

  private final String message;
  private final String correlationKey;
  private final Map<String, Object> variables;
  private final String payloadTemplate;
  private final long time;

  public MessageAction(
      String message,
      String correlationKey,
      String payloadTemplate,
      Map<String, Object> variables,
      ZeebeService zeebeService,
      long time) {
    super();
    this.message = message;
    this.correlationKey = correlationKey;
    this.variables = variables;
    this.payloadTemplate = payloadTemplate;
    this.setZeebeService(zeebeService);
    this.time = time;
  }

  @Override
  public void run() {
    ContextUtils.setProcessInstanceTime((String) variables.get(Constants.UNIQUE_ID_KEY), time);
    this.getZeebeService()
        .message(message, correlationKey, getVariables(this.payloadTemplate, variables));
  }

  @Override
  public String getType() {
    return "Message";
  }
}
