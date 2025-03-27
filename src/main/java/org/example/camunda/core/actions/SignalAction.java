package org.example.camunda.core.actions;

import java.util.Map;
import org.example.camunda.Constants;
import org.example.camunda.core.ZeebeService;
import org.example.camunda.utils.ContextUtils;

public class SignalAction extends Action {

  private final String signal;
  private final Map<String, Object> variables;
  private final String payloadTemplate;
  private final long time;

  public SignalAction(
      String signal,
      String payloadTemplate,
      Map<String, Object> variables,
      ZeebeService zeebeService,
      long time) {
    super();
    this.signal = signal;
    this.variables = variables;
    this.payloadTemplate = payloadTemplate;
    this.setZeebeService(zeebeService);
    this.time = time;
  }

  @Override
  public void run() {
    ContextUtils.setProcessInstanceTime((String) variables.get(Constants.UNIQUE_ID_KEY), time);
    this.getZeebeService().signal(signal, getVariables(this.payloadTemplate, variables));
  }

  @Override
  public String getType() {
    return "Signal";
  }
}
