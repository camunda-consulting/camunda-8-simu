package org.example.camunda.core.actions;

import org.example.camunda.core.ZeebeService;
import org.example.camunda.dto.IncidentTypeEnum;

public class IncidentJobAction extends Action {

  private Long jobKey;
  private IncidentTypeEnum incident;

  public IncidentJobAction(Long jobKey, IncidentTypeEnum incident, ZeebeService zeebeService) {
    super();
    this.jobKey = jobKey;
    this.incident = incident;
    this.setZeebeService(zeebeService);
  }

  @Override
  public void run() {
    this.getZeebeService().incidentJob(this.jobKey, incident);
  }
}
