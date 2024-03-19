package org.example.camunda.core.actions;

import java.util.Map;
import org.example.camunda.core.ZeebeService;
import org.example.camunda.core.model.Scenario;

public class StartInstancesAction extends Action {

  private Scenario scenario;
  private Map<Long, Scenario> scenarioMap;

  public StartInstancesAction(
      Scenario scenario, ZeebeService zeebeService, Map<Long, Scenario> scenarioMap) {
    super();
    this.scenario = scenario;
    this.setZeebeService(zeebeService);
    this.scenarioMap = scenarioMap;
  }

  @Override
  public void run() {

    try {
      Long processInstanceKey =
          this.getZeebeService()
              .startProcessInstance(
                  this.scenario.getBpmnProcessId(),
                  this.scenario.getVersion(),
                  getVariables(this.scenario.getStartPayload(), null));
      scenarioMap.put(processInstanceKey, this.scenario);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
