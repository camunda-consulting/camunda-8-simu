package org.example.camunda.core.actions;

import java.util.Map;
import org.example.camunda.Constants;
import org.example.camunda.core.ZeebeService;
import org.example.camunda.dto.IncidentTypeEnum;
import org.example.camunda.utils.ContextUtils;

public class IncidentJobAction extends Action {

  private final Long jobKey;
  private final IncidentTypeEnum incident;
  private final Map<String, Object> variables;
  private final long time;

  public IncidentJobAction(
      Long jobKey,
      IncidentTypeEnum incident,
      Map<String, Object> variables,
      ZeebeService zeebeService,
      long time) {
    super();
    this.jobKey = jobKey;
    this.incident = incident;
    this.variables = variables;
    this.setZeebeService(zeebeService);
    this.time = time;
  }

  @Override
  public void run() {
    ContextUtils.setProcessInstanceTime((String) variables.get(Constants.UNIQUE_ID_KEY), time);
    this.getZeebeService().incidentJob(this.jobKey, incident);
  }

  @Override
  public String getType() {
    return "Incident";
  }
}
