package org.example.camunda.facade;

import java.io.IOException;
import java.util.Map;
import org.example.camunda.service.ScenarioExecService;
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

  @Autowired private ScenarioExecService scenarioExecService;

  // @Autowired StartPiScheduler piScheduler;
  // @Autowired Simu simu;
  // @Autowired ScenarioExecutor scenarioExecutor;

  /*@GetMapping("/start")
  public void start() {
    List<Scenario> scenarii = simu.getScenarios();
    for (Scenario s : scenarii) {
      scenarioExecutor.addScenario(s);
    }
    scenarioExecutor.execute();
    // piScheduler.start();
  }*/

  @PostMapping("/record/{dueTimestamp}")
  public void catchIntermediateEvent(
      @PathVariable Long dueTimestamp, @RequestBody Map<String, Object> processInstanceRecordValue)
      throws IOException {
    LOG.info("catch event due at timestamp " + dueTimestamp);
    if (!timers.equals("operate")) {

      scenarioExecService.handleIntermediateEvent(dueTimestamp);
    }
  }

  @GetMapping("/engine/idle")
  public void catchIdle() {
    if (!timers.equals("operate")) {
      scenarioExecService.nextTimedAction();
      // scenarioExecutor.nextTimedActions();
    }
  }

  /*@GetMapping("/stop")
  public void stop() {
    piScheduler.stop();
  }*/
}
