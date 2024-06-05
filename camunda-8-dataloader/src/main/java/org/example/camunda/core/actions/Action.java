package org.example.camunda.core.actions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import org.example.camunda.core.PayloadGenerator;
import org.example.camunda.core.ZeebeService;
import org.example.camunda.utils.JsonUtils;

public abstract class Action implements Runnable {
  private ZeebeService zeebeService;

  public ZeebeService getZeebeService() {
    return zeebeService;
  }

  public void setZeebeService(ZeebeService zeebeService) {
    this.zeebeService = zeebeService;
  }

  public static ObjectNode getVariables(String payloadTemplate, Map<String, Object> variables) {
    return JsonUtils.toJsonNode(PayloadGenerator.generatePayload(payloadTemplate, variables));
  }
}
