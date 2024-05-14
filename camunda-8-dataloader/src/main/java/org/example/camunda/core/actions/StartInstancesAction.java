package org.example.camunda.core.actions;

import java.util.Map;
import org.example.camunda.core.ZeebeService;
import org.example.camunda.dto.Scenario;

public class StartInstancesAction extends Action {

  private Scenario scenario;
  private long nbInstances;
  private Map<Long, Scenario> scenarioMap;

  public StartInstancesAction(
      Scenario scenario,
      long nbInstances,
      ZeebeService zeebeService,
      Map<Long, Scenario> scenarioMap) {
    super();
    this.scenario = scenario;
    this.nbInstances = nbInstances;
    this.setZeebeService(zeebeService);
    this.scenarioMap = scenarioMap;
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
        scenarioMap.put(processInstanceKey, this.scenario);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
