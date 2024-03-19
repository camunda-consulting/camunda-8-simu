package org.example.camunda.service;

import io.camunda.operate.exception.OperateException;
import io.camunda.operate.model.FlowNodeInstance;
import io.camunda.operate.model.ProcessInstance;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ModifyProcessInstanceCommandStep1.ModifyProcessInstanceCommandStep3;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.DocumentException;
import org.example.camunda.dto.ProcessInstanceMigration;
import org.example.camunda.utils.BpmnUtils;
import org.example.camunda.utils.MigrationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MigrationService {

  @Autowired private ZeebeClient zeebeClient;
  @Autowired private OperateService operateService;

  private static final Map<Long, Integer> TARGET_VERSION_MAP = new HashMap<>();
  private static final Map<Long, String> MODIFIED_SUB_PROCESSES = new HashMap<>();

  public List<ProcessInstanceMigration> copyMainInstances(List<Long> processInstanceKeys)
      throws OperateException {
    processInstanceKeys.sort(null);
    List<ProcessInstanceMigration> result = new ArrayList<>();
    for (Long key : processInstanceKeys) {
      ProcessInstanceMigration migration = copyMainInstance(key);
      if (migration != null) {
        result.add(migration);
      }
    }
    return result;
  }

  public ProcessInstanceMigration copyMainInstance(Long processInstanceKey)
      throws OperateException {
    List<FlowNodeInstance> flowNodes = operateService.listActiveFlowNodes(processInstanceKey);
    if (flowNodes.isEmpty()) {
      return null;
    }
    ProcessInstance instance = operateService.getInstance(processInstanceKey);
    Map<String, Object> variables = operateService.getVariables(processInstanceKey);
    variables.put("originalProcessInstanceKey", processInstanceKey);

    Integer version = getTargetProcessVersion(instance);

    FlowNodeInstance flowNode = flowNodes.get(0);
    Long childInstanceKey = prepareCallActivityRestoration(flowNode);
    if (childInstanceKey != null) {
      variables.put("childInstanceKey", childInstanceKey);
    }
    ProcessInstanceEvent result =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId(instance.getBpmnProcessId())
            .version(version)
            .startBeforeElement(flowNode.getFlowNodeId())
            .variables(variables)
            .send()
            .join();
    for (int i = 1; i < flowNodes.size(); i++) {
      flowNode = flowNodes.get(i);
      childInstanceKey = prepareCallActivityRestoration(flowNode);
      ModifyProcessInstanceCommandStep3 cmd =
          zeebeClient
              .newModifyProcessInstanceCommand(result.getProcessInstanceKey())
              .activateElement(flowNode.getFlowNodeId());
      if (childInstanceKey != null) {
        cmd =
            cmd.withVariables(
                Map.of("childInstanceKey", childInstanceKey), flowNode.getFlowNodeId());
      }
      cmd.send().join();
    }
    return new ProcessInstanceMigration()
        .setOriginalProcessInstanceKey(processInstanceKey)
        .setOriginalProcessInstance(instance)
        .setActiveFlowNodes(flowNodes)
        .setVariables(variables)
        .setNewProcessInstanceKey(result.getProcessInstanceKey());
  }

  private Long prepareCallActivityRestoration(FlowNodeInstance flowNode) throws OperateException {
    if (flowNode.getType().equals("CALL_ACTIVITY")) {
      ProcessInstance childInstance =
          operateService.getSubProcessInstance(flowNode.getProcessInstanceKey(), flowNode.getKey());
      Long currentProcessDef = childInstance.getProcessDefinitionKey();
      if (!MODIFIED_SUB_PROCESSES.containsKey(currentProcessDef)) {
        String xmlDef = operateService.getProcessDefinitionXmlByKey(currentProcessDef);
        try {
          String modified = MigrationUtils.insertMigrationStep(xmlDef);
          MODIFIED_SUB_PROCESSES.put(currentProcessDef, modified);
        } catch (DocumentException e) {
          throw new OperateException("Error modifying the process called from call activity", e);
        }
      }
      String def = MODIFIED_SUB_PROCESSES.get(currentProcessDef);
      String processName = BpmnUtils.getProcessName(def, childInstance.getBpmnProcessId());
      DeploymentEvent deployment =
          zeebeClient
              .newDeployResourceCommand()
              .addResourceString(def, StandardCharsets.UTF_8, processName + ".bpmn")
              .send()
              .join();
      return childInstance.getKey();
    }
    return null;
  }

  private Integer getTargetProcessVersion(ProcessInstance instance) throws OperateException {
    Long definitionKey = instance.getProcessDefinitionKey();
    if (!TARGET_VERSION_MAP.containsKey(definitionKey)) {
      String xmlDef = operateService.getProcessDefinitionXmlByKey(definitionKey);
      String processName = BpmnUtils.getProcessName(xmlDef, instance.getBpmnProcessId());
      DeploymentEvent deployment =
          zeebeClient
              .newDeployResourceCommand()
              .addResourceString(xmlDef, StandardCharsets.UTF_8, processName + ".bpmn")
              .send()
              .join();
      int newVersion = deployment.getProcesses().get(0).getVersion();
      TARGET_VERSION_MAP.put(definitionKey, newVersion);
    }
    return TARGET_VERSION_MAP.get(definitionKey);
  }

  public boolean restoreInstanceState(ActivatedJob job, Long originalInstanceKey)
      throws OperateException {
    Long processInstanceKey = job.getProcessInstanceKey();
    List<FlowNodeInstance> flowNodes = operateService.listActiveFlowNodes(originalInstanceKey);
    if (flowNodes.isEmpty()) {
      return false;
    }
    for (int i = 0; i < flowNodes.size(); i++) {
      FlowNodeInstance flowNode = flowNodes.get(i);
      Long childInstanceKey = prepareCallActivityRestoration(flowNode);
      ModifyProcessInstanceCommandStep3 cmd =
          zeebeClient
              .newModifyProcessInstanceCommand(processInstanceKey)
              .activateElement(flowNode.getFlowNodeId());
      if (childInstanceKey != null) {
        cmd =
            cmd.withVariables(
                Map.of("childInstanceKey", childInstanceKey), flowNode.getFlowNodeId());
      }
      cmd.send().join();
    }
    return true;
  }
}
