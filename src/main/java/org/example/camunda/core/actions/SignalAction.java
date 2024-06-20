package org.example.camunda.core.actions;

import java.util.Map;
import org.example.camunda.core.ZeebeService;

public class SignalAction extends Action {

  private String signal;
  private Map<String, Object> variables;
  private String payloadTemplate;

  public SignalAction(
      String signal,
      String payloadTemplate,
      Map<String, Object> variables,
      ZeebeService zeebeService) {
    super();
    this.signal = signal;
    this.variables = variables;
    this.payloadTemplate = payloadTemplate;
    this.setZeebeService(zeebeService);
  }

  @Override
  public void run() {
    this.getZeebeService().signal(signal, getVariables(this.payloadTemplate, variables));
  }
}
