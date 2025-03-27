package org.example.camunda.core.actions;

import java.util.Map;
import org.example.camunda.Constants;
import org.example.camunda.core.ZeebeService;
import org.example.camunda.utils.ContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompleteJobAction extends Action {
  private static final Logger LOG = LoggerFactory.getLogger(CompleteJobAction.class);

  private final Long jobKey;
  private final Map<String, Object> variables;
  private final String payloadTemplate;
  private final long time;

  public CompleteJobAction(
      Long jobKey,
      String payloadTemplate,
      Map<String, Object> variables,
      ZeebeService zeebeService,
      long time) {
    super();
    this.jobKey = jobKey;
    this.variables = variables;
    this.payloadTemplate = payloadTemplate;
    this.setZeebeService(zeebeService);
    this.time = time;
  }

  @Override
  public void run() {
    ContextUtils.setProcessInstanceTime((String) variables.get(Constants.UNIQUE_ID_KEY), time);
    this.getZeebeService().completeJob(this.jobKey, getVariables(this.payloadTemplate, variables));
  }

  @Override
  public String getType() {
    return "Complete";
  }
}
