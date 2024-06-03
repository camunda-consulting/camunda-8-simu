package org.example.camunda.core.actions;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.camunda.core.ZeebeService;
import org.example.camunda.dto.Scenario;
import org.example.camunda.utils.ContextUtils;
import org.example.camunda.utils.HistoUtils;

public class StartInstancesAction extends Action {

  private Scenario scenario;
  private long nbInstances;
  private Double progress;

  public StartInstancesAction(
      Scenario scenario, long nbInstances, ZeebeService zeebeService, Double progress) {
    super();
    this.scenario = scenario;
    this.nbInstances = nbInstances;
    this.setZeebeService(zeebeService);
    this.progress = progress;
  }

  @Override
  public void run() {

    try {
      for (long x = 0; x < this.nbInstances; x++) {
        JsonNode variables = getVariables(this.scenario.getJsonTemplate(), null);
        Long processInstanceKey =
            this.getZeebeService()
                .startProcessInstance(
                    this.scenario.getBpmnProcessId(), this.scenario.getVersion(), variables);
        ContextUtils.addInstance(processInstanceKey, this.scenario, this.progress);
      }
      HistoUtils.addHisto("Started " + this.nbInstances + " instances.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
