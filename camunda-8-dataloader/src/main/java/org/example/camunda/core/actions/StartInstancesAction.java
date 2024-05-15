package org.example.camunda.core.actions;

import org.example.camunda.core.ZeebeService;
import org.example.camunda.dto.Scenario;
import org.example.camunda.utils.ContextUtils;

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
        Long processInstanceKey =
            this.getZeebeService()
                .startProcessInstance(
                    this.scenario.getBpmnProcessId(),
                    this.scenario.getVersion(),
                    getVariables(this.scenario.getJsonTemplate(), null));
        ContextUtils.addInstance(processInstanceKey, this.scenario, this.progress);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
