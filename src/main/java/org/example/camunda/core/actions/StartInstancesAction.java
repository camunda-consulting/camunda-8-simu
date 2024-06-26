package org.example.camunda.core.actions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.UUID;
import org.example.camunda.core.ZeebeService;
import org.example.camunda.dto.InstanceStartTypeEnum;
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
        ObjectNode variables = getVariables(this.scenario.getJsonTemplate().getTemplate(), null);
        String uniqueId = UUID.randomUUID().toString();
        ContextUtils.addInstance(uniqueId, this.scenario, this.progress);
        variables.put("uniqueProcessIdentifier", uniqueId);
        if (InstanceStartTypeEnum.MSG == this.scenario.getStartType()) {
          this.getZeebeService().message(this.scenario.getStartMsgName(), uniqueId, variables);
        } else {
          this.getZeebeService()
              .startProcessInstance(
                  this.scenario.getBpmnProcessId(), this.scenario.getVersion(), variables);
        }
      }
      HistoUtils.addHisto(
          "Started "
              + this.nbInstances
              + " instances (type = "
              + this.scenario.getStartType()
              + ").");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
