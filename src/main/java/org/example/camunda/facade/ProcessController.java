package org.example.camunda.facade;

import io.camunda.operate.exception.OperateException;
import io.camunda.operate.model.FlowNodeInstance;
import io.camunda.operate.model.ProcessDefinition;
import io.camunda.operate.model.ProcessInstance;
import io.camunda.operate.model.ProcessInstanceState;
import java.util.List;
import org.example.camunda.service.OperateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/process")
public class ProcessController {

  private static final Logger LOG = LoggerFactory.getLogger(ProcessController.class);
  private final OperateService operateService;

  public ProcessController(OperateService operateService) {
    this.operateService = operateService;
  }

  @GetMapping("/definitions")
  public List<ProcessDefinition> definitions() throws OperateException {
    return operateService.getProcessDefinitions();
  }

  @GetMapping("/instances/{state}")
  public List<ProcessInstance> instances(@PathVariable ProcessInstanceState state)
      throws OperateException {
    return operateService.listInstances(state);
  }

  @GetMapping("/instances/{key}/flowNodes")
  public List<FlowNodeInstance> activeFlowNode(@PathVariable Long key) throws OperateException {
    return operateService.listActiveFlowNodes(key);
  }
}
