package org.example.camunda.facade;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.example.camunda.core.ScenarioExecutor;
import org.example.camunda.core.StartPiScheduler;
import org.example.camunda.core.model.Scenario;
import org.example.camunda.simu.Simu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/load")
public class DataLoadController {
  private static final Logger LOG = LoggerFactory.getLogger(DataLoadController.class);

  @Value("${timers.from:operate}")
  private String timers;

  @Autowired StartPiScheduler piScheduler;
  @Autowired Simu simu;
  @Autowired ScenarioExecutor scenarioExecutor;

  @GetMapping("/start")
  public void start() {
    List<Scenario> scenarii = simu.getScenarios();
    for (Scenario s : scenarii) {
      scenarioExecutor.addScenario(s);
    }
    scenarioExecutor.execute();
    // piScheduler.start();
  }

  @PostMapping("/record/{timestamp}")
  public void catchIntermediateEvent(
      @PathVariable Long timestamp, @RequestBody Map<String, Object> processInstanceRecordValue)
      throws IOException {
    // LOG.info("catch event at timestamp " + timestamp);
    // LOG.info(JsonUtils.toJson(processInstanceRecordValue));

    // {"tenantId":"<default>","bpmnProcessId":"simpleDataLoad","processDefinitionKey":2251799813685249,"processInstanceKey":2251799813687096,"elementId":"Event_1k51mvz","flowScopeKey":2251799813687096,"bpmnEventType":"TIMER","parentProcessInstanceKey":-1,"parentElementInstanceKey":-1,"bpmnElementType":"INTERMEDIATE_CATCH_EVENT","bpmnProcessIdBuffer":{"expandable":false},"elementIdBuffer":{"expandable":false},"version":1,"encodedLength":275,"length":275,"empty":false}
    if (!timers.equals("operate")) {
      // timestamp is not relyable as not based on internal clock
      scenarioExecutor.executeIntermediateEvent(
          (Long) processInstanceRecordValue.get("processInstanceKey"),
          (String) processInstanceRecordValue.get("elementId"));
    }
  }

  @GetMapping("/engine/idle")
  public void catchIdle() {
    if (!timers.equals("operate")) {
      scenarioExecutor.nextTimedActions();
    }
  }

  @GetMapping("/stop")
  public void stop() {
    piScheduler.stop();
  }
}
