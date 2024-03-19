package org.example.camunda.core.actions;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
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

  public static JsonNode getVariables(String payloadTemplate, Map<String, Object> variables)
      throws IOException {
    return JsonUtils.toJsonNode(PayloadGenerator.generatePayload(payloadTemplate, variables));
  }
}
