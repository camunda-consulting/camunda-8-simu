package org.example.camunda.facade;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.example.camunda.dto.ExecutionPlan;
import org.example.camunda.dto.HistoPlan;
import org.example.camunda.dto.Scenario;
import org.example.camunda.dto.operate.ProcessDefinition;
import org.example.camunda.dto.progression.Evolution;
import org.example.camunda.service.ExecutionPlanService;
import org.example.camunda.service.ScenarioExecService;
import org.example.camunda.test.TestAction;
import org.example.camunda.test.ZeebeTestUtils;
import org.example.camunda.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/plan")
public class ExecPlanController {

  private static final Logger LOG = LoggerFactory.getLogger(ExecPlanController.class);
  private final ExecutionPlanService execPlanService;
  private final ScenarioExecService scenarioExecService;

  public ExecPlanController(
      ExecutionPlanService execPlanService, ScenarioExecService scenarioExecService) {
    this.execPlanService = execPlanService;
    this.scenarioExecService = scenarioExecService;
  }

  @GetMapping("/running")
  public ExecutionPlan runningPlan() {
    if (ContextUtils.isExecuting()) {
      return ContextUtils.getPlan();
    }
    return null;
  }

  @GetMapping("/stop")
  public void stopPlan() {
    if (ContextUtils.getPlan() != null) {
      this.scenarioExecService.stop();
    }
  }

  @GetMapping("/{name}")
  public ExecutionPlan getPlan(@PathVariable String name)
      throws IOException {
    return execPlanService.find(name);
  }

  @PostMapping("/{bpmnProcessId}/xml")
  public ExecutionPlan updateXml(@PathVariable String bpmnProcessId, @RequestBody String xml)
      throws IOException {
    ExecutionPlan plan = execPlanService.find(bpmnProcessId);
    plan.setXml(xml);
    plan.setXmlModified(true);
    execPlanService.save(plan);
    return plan;
  }

  @PutMapping("/newScenario")
  public ExecutionPlan newScenario(@RequestBody ExecutionPlan plan) {
    // ExecutionPlan originalPlan = execPlanService.find(bpmnProcessId);
    Scenario s = ScenarioUtils.generateScenario(plan.getXml(), plan);
    s.setName("Scenario " + (plan.getScenarii().size() + 1));
    plan.getScenarii().add(s);
    return plan;
  }

  @PutMapping
  public ExecutionPlan save(@RequestBody ExecutionPlan plan) throws IOException {
    return execPlanService.save(plan);
  }

  @GetMapping("/{name}/start")
  public boolean startPlan(@PathVariable String name)
      throws IOException {
    ExecutionPlan plan = execPlanService.find(name);
    if (plan == null) {
      return false;
    }
    this.scenarioExecService.start(plan);
    return true;
  }

  @PostMapping("/start")
  public boolean startPlan(
      @RequestBody ExecutionPlan plan,
      @RequestParam(value = "scenario", required = false) String scenario)
      throws IOException {
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

  @PostMapping("/test")
  public Map<String, List<TestAction>> testPlan(@RequestBody ExecutionPlan plan)
      throws NoSuchFieldException, IllegalAccessException {
    if (plan == null) {
      return null;
    }
    return ZeebeTestUtils.test(plan);
  }

  @GetMapping("/status/{name}")
  public Map<String, String> getSatus(@PathVariable String name) {
    HistoPlan plan = HistoUtils.getPlan(name);
    if (plan == null) {
      return Map.of("status", "not started");
    }
    return Map.of("status", plan.isRunning() ? "running" : "completed");
  }

  @PostMapping
  public ExecutionPlan createPlanFromXml(@RequestBody String xml) throws IOException {
    ProcessDefinition def = new ProcessDefinition();
    Map<String, String> procIdName = BpmnUtils.getProcessIdAndName(xml);
    def.setBpmnProcessId(procIdName.keySet().iterator().next());
    def.setVersion(-1L);
    def.setName(procIdName.values().iterator().next());
    ExecutionPlan plan = new ExecutionPlan();
    plan.setName(def.getBpmnProcessId());
    plan.setDefinition(def);
    plan.setXml(xml);
    Scenario s = ScenarioUtils.generateScenario(xml, plan);
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

  @PostMapping("/preview/plan")
  public Map<String, Object> evolDatasetPreview(@RequestBody ExecutionPlan plan) {
    ZonedDateTime firstDay = null;
    ZonedDateTime lastDay = null;
    for (Scenario scenario : plan.getScenarii()) {
      ZonedDateTime firstDayTmp =
          ScenarioUtils.getZonedDateDay(scenario.getFirstDayFeelExpression());
      ZonedDateTime lastDayTmp = ScenarioUtils.getZonedDateDay(scenario.getLastDayFeelExpression());
      if (firstDay == null || firstDay.isAfter(firstDayTmp)) {
        firstDay = firstDayTmp;
      }
      if (lastDay == null || lastDay.isBefore(lastDayTmp)) {
        lastDay = lastDayTmp;
      }
    }
    lastDay = lastDay.plusDays(1);
    List<String> labels = new ArrayList<>();
    List<Long> cumulativeValues = new ArrayList<>();
    ZonedDateTime parcoursDay = firstDay.plusNanos(1);
    while (parcoursDay.isBefore(lastDay)) {
      labels.add(parcoursDay.format(DateTimeFormatter.ISO_OFFSET_DATE).substring(0, 10));
      cumulativeValues.add(0L);
      parcoursDay = parcoursDay.plusDays(1);
    }

    List<Map<String, Object>> datasets = new ArrayList<>();
    for (Scenario scenario : plan.getScenarii()) {
      ZonedDateTime firstDayTmp =
          ScenarioUtils.getZonedDateDay(scenario.getFirstDayFeelExpression());
      ZonedDateTime lastDayTmp = ScenarioUtils.getZonedDateDay(scenario.getLastDayFeelExpression());
      parcoursDay = firstDay.plusNanos(1);
      List<Long> values = new ArrayList<>();
      int i = 0;
      while (parcoursDay.isBefore(firstDayTmp)) {
        values.add(0L);
        i++;
        parcoursDay = parcoursDay.plusDays(1);
      }
      long durationInDays = ChronoUnit.DAYS.between(firstDayTmp, lastDayTmp) + 1;
      for (double x = 0; x < durationInDays; x++) {
        Long nb =
            ScenarioUtils.calculateInstancesPerDay(scenario.getEvolution(), x / durationInDays);
        values.add(nb);
        cumulativeValues.set(i, cumulativeValues.get(i) + nb);
        i++;
      }
      parcoursDay = lastDayTmp.plusNanos(1);
      while (parcoursDay.isBefore(lastDay)) {
        values.add(0L);
        parcoursDay = parcoursDay.plusDays(1);
      }
      datasets.add(
          Map.of(
              "type",
              "line",
              "label",
              scenario.getName(),
              "data",
              values,
              "fill",
              false,
              "tension",
              0.3));
    }
    datasets.add(
        Map.of(
            "type",
            "bar",
            "label",
            "cumulative",
            "data",
            cumulativeValues,
            "fill",
            false,
            "tension",
            0.3));
    return Map.of("labels", labels, "datasets", datasets);
  }

  @DeleteMapping("/{name}")
  public Map<String, Boolean> delete(@PathVariable String name) throws IOException {
    execPlanService.delete(name);
    return Map.of("status", true);
  }
}
