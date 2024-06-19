package org.example.camunda.facade;

import io.camunda.operate.exception.OperateException;
import io.camunda.operate.model.ProcessDefinition;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.example.camunda.dto.ExecutionPlan;
import org.example.camunda.dto.Scenario;
import org.example.camunda.dto.progression.Evolution;
import org.example.camunda.service.ExecutionPlanService;
import org.example.camunda.service.OperateService;
import org.example.camunda.service.ScenarioExecService;
import org.example.camunda.utils.BpmnUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/plan")
public class ExecPlanController {

  private static final Logger LOG = LoggerFactory.getLogger(ExecPlanController.class);
  private final OperateService operateService;
  private final ExecutionPlanService execPlanService;
  private final ScenarioExecService scenarioExecService;

  public ExecPlanController(
      OperateService operateService,
      ExecutionPlanService execPlanService,
      ScenarioExecService scenarioExecService) {
    this.operateService = operateService;
    this.execPlanService = execPlanService;
    this.scenarioExecService = scenarioExecService;
  }

  @GetMapping("/running")
  public ExecutionPlan runningPlan() {
    return ContextUtils.getPlan();
  }

  @GetMapping("/stop")
  public void stopPlan() {
    if (ContextUtils.getPlan() != null) {
      this.scenarioExecService.stop();
    }
  }

  @GetMapping("/{bpmnProcessId}/{version}")
  public ExecutionPlan definitions(@PathVariable String bpmnProcessId, @PathVariable Long version)
      throws OperateException, IOException {
    ExecutionPlan plan = execPlanService.find(bpmnProcessId, version);
    if (plan == null) {
      plan = new ExecutionPlan();

      plan.setDefinition(operateService.getProcessDefinition(bpmnProcessId, version));
      plan.setXml(operateService.getProcessDefinitionXmlByKey(plan.getDefinition().getKey()));
      plan.setXmlDependencies(operateService.getDependencies(plan.getXml()));
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

  @PutMapping("/{bpmnProcessId}/{version}/newScenario")
  public ExecutionPlan newScenario(
      @PathVariable String bpmnProcessId,
      @PathVariable Long version,
      @RequestBody ExecutionPlan plan)
      throws OperateException, IOException {
    ExecutionPlan originalPlan = execPlanService.find(bpmnProcessId, version);
    Scenario s = ScenarioUtils.generateScenario(originalPlan.getXml());
    s.setName("Scenario " + (plan.getScenarii().size() + 1));
    plan.getScenarii().add(s);
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
  public boolean startPlan(@PathVariable String bpmnProcessId, @PathVariable Long version)
      throws OperateException, IOException {
    ExecutionPlan plan = execPlanService.find(bpmnProcessId, version);
    if (plan == null) {
      return false;
    }
    if (plan.getXmlModified()) {
      this.scenarioExecService.deploy(plan.getDefinition().getName(), plan.getXml());
    }
    this.scenarioExecService.start(plan);
    return true;
  }

  @PostMapping("/start")
  public boolean startPlan(
      @RequestBody ExecutionPlan plan,
      @RequestParam(value = "scenario", required = false) String scenario)
      throws OperateException, IOException {
    if (plan == null) {
      return false;
    }
    if (scenario != null) {
      this.scenarioExecService.start(plan, scenario);
    } else {
      this.scenarioExecService.start(plan);
    }
    return true;
  }

  @PostMapping
  public ExecutionPlan createPlanFromXml(@RequestBody String xml) throws IOException {
    ProcessDefinition def = new ProcessDefinition();
    Map<String, String> procIdName = BpmnUtils.getProcessIdAndName(xml);
    def.setBpmnProcessId(procIdName.keySet().iterator().next());
    def.setVersion(-1L);
    def.setName(procIdName.values().iterator().next());
    ExecutionPlan plan = new ExecutionPlan();
    plan.setDefinition(def);
    plan.setXml(xml);
    Scenario s = ScenarioUtils.generateScenario(xml);
    s.setName("Scenario 1");
    plan.getScenarii().add(s);
    return execPlanService.save(plan);
  }

  @GetMapping
  public List<String> list() {
    return execPlanService.list();
  }

  @PostMapping("/preview/evol")
  public Map<String, Object> evolDatasetPreview(@RequestBody Evolution evol) {
    List<String> labels = new ArrayList<>();
    List<Long> values = new ArrayList<>();

    labels.add("0.00");
    values.add(ScenarioUtils.calculateInstancesPerDay(evol, 0));
    labels.add("0.05");
    values.add(ScenarioUtils.calculateInstancesPerDay(evol, 0.05));
    for (int x = 10; x < 100; x += 5) {
      labels.add("0." + x);
      values.add(ScenarioUtils.calculateInstancesPerDay(evol, x / 100.0));
    }
    return Map.of(
        "labels",
        labels,
        "datasets",
        List.of(
            Map.of(
                "label",
                "preview",
                "data",
                values,
                "fill",
                false,
                "borderColor",
                "rgb(75, 192, 192)",
                "tension",
                0.3)));
  }
}
