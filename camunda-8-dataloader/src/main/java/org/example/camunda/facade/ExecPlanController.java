package org.example.camunda.facade;

import io.camunda.operate.exception.OperateException;
import java.io.IOException;
import java.util.List;
import org.example.camunda.dto.ExecutionPlan;
import org.example.camunda.dto.Scenario;
import org.example.camunda.service.ExecutionPlanService;
import org.example.camunda.service.OperateService;
import org.example.camunda.service.ScenarioExecService;
import org.example.camunda.utils.ContextUtils;
import org.example.camunda.utils.ScenarioUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/plan")
public class ExecPlanController {

  private static final Logger LOG = LoggerFactory.getLogger(ExecPlanController.class);
  private final OperateService operateService;
  private final ExecutionPlanService execPlanService;
  private final ScenarioExecService ScenarioExecService;

  public ExecPlanController(
      OperateService operateService,
      ExecutionPlanService execPlanService,
      ScenarioExecService ScenarioExecService) {
    this.operateService = operateService;
    this.execPlanService = execPlanService;
    this.ScenarioExecService = ScenarioExecService;
  }

  @GetMapping("/{bpmnProcessId}/{version}")
  public ExecutionPlan definitions(@PathVariable String bpmnProcessId, @PathVariable Long version)
      throws OperateException, IOException {
    ExecutionPlan plan = execPlanService.find(bpmnProcessId, version);
    if (plan == null) {
      plan = new ExecutionPlan();

      plan.setDefinition(operateService.getProcessDefinition(bpmnProcessId, version));
      plan.setXml(operateService.getProcessDefinitionXmlByKey(plan.getDefinition().getKey()));
      Scenario s = ScenarioUtils.generateScenario(plan.getXml());
      s.setName("Scenario 1");
      plan.getScenarii().add(s);
      execPlanService.save(plan);
    }
    return plan;
  }

  @PostMapping("/{bpmnProcessId}/{version}/xml")
  public ExecutionPlan definitions(
      @PathVariable String bpmnProcessId, @PathVariable Long version, @RequestBody String xml)
      throws OperateException, IOException {
    ExecutionPlan plan = execPlanService.find(bpmnProcessId, version);
    plan.setXml(xml);
    plan.setXmlModified(true);
    execPlanService.save(plan);
    return plan;
  }

  @PutMapping("/{bpmnProcessId}/{version}")
  public ExecutionPlan definitions(
      @PathVariable String bpmnProcessId,
      @PathVariable Long version,
      @RequestBody ExecutionPlan plan)
      throws OperateException, IOException {
    return execPlanService.save(plan);
  }

  @GetMapping("/{bpmnProcessId}/{version}/start")
  public void startPlan(@PathVariable String bpmnProcessId, @PathVariable Long version)
      throws OperateException, IOException {
    ExecutionPlan plan = execPlanService.find(bpmnProcessId, version);
    if (plan.getXmlModified()) {
      this.ScenarioExecService.deploy(plan.getDefinition().getName(), plan.getXml());
    }
    this.ScenarioExecService.start(plan);
  }

  @GetMapping("/{bpmnProcessId}/{version}/scenario/{scenarioName}/start")
  public void startScenario(
      @PathVariable String bpmnProcessId,
      @PathVariable Long version,
      @PathVariable String scenarioName)
      throws OperateException, IOException {
    ExecutionPlan plan = execPlanService.find(bpmnProcessId, version);
    if (plan.getXmlModified()) {
      this.ScenarioExecService.deploy(plan.getDefinition().getName(), plan.getXml());
    }
    this.ScenarioExecService.start(plan, scenarioName);
  }

  @GetMapping("/histo")
  public List<String> histo() {
    return ContextUtils.getHisto();
  }
}
