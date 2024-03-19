package org.example.camunda.simu;

import java.util.ArrayList;
import java.util.List;
import org.example.camunda.core.model.Scenario;
import org.example.camunda.core.model.Step;
import org.example.camunda.core.model.StepType;
import org.springframework.stereotype.Service;

@Service
public class Simu {
  private int scenarioIdx = 0;

  public List<Scenario> getScenarios() {
    List<Scenario> result = new ArrayList<>();
    /*for (long i = 0; i < 4000; i++) {
      if (i % 4 == 0) {
        result.add(buildScenario2(i * 2592000));
      } else {
        result.add(buildScenario1(i * 2592000));
      }
    }*/

    for (long i = 0; i < 60; i++) {
      long nbInstances = 10 + Math.round(Math.random() * i);
      for (long j = 0; j < nbInstances; j++) {
        if (j % 4 == 0) {
          result.add(buildScenario2(i * 86400000));
          result.add(buildScenario2(i * 12400000));
        } else {
          result.add(buildScenario1(i * 96400000));
          result.add(buildScenario1(i * 14400000));
        }
      }
    }
    return result;
  }

  public Scenario buildScenario1(long when) {
    Scenario scenario = new Scenario();
    scenario.setBpmnProcessId("simpleDataLoad");
    scenario.addStep(buildStartStep1(when));
    scenario.addStep(buildAuto(3 + Math.round(Math.random() * 5)));
    scenario.addStep(buildTimer(86400000l));
    scenario.addStep(buildLastTask(0));
    return scenario;
  }

  public Scenario buildScenario2(long when) {
    Scenario scenario = new Scenario();
    scenario.setBpmnProcessId("simpleDataLoad");
    scenario.addStep(buildStartStep2(when));
    scenario.addStep(buildUserTask(180000 + Math.round(Math.random() * 270000)));
    scenario.addStep(buildTimer(86400000l));
    scenario.addStep(buildLastTask(0));
    return scenario;
  }

  private Step buildTimer(long when) {
    Step step = new Step();
    step.setType(StepType.INTERMEDIATE_TIMER);
    step.setTimeBeforeCompletion(when);
    step.setElementId("Event_1k51mvz");
    return step;
  }

  private Step buildAuto(long when) {
    Step step = new Step();
    step.setType(StepType.TASK);
    step.setElementId("Activity_0rb9s4p");
    step.setTemplateVariables("{\"lastname\":\"[( ${templateUtils.lastname} )]\"}");
    step.setShouldComplete(true);
    step.setTimeBeforeCompletion(when);
    return step;
  }

  private Step buildUserTask(long when) {
    Step step = new Step();
    step.setType(StepType.TASK);
    step.setElementId("Activity_00mbrcd");
    step.setTemplateVariables(
        "{\"assignee\":\"[( ${templateUtils.lastname} )]\", \"lastname\":\"[( ${templateUtils.lastname} )]\"}");
    step.setShouldComplete(true);
    step.setTimeBeforeCompletion(when);
    return step;
  }

  private Step buildLastTask(long when) {
    Step step = new Step();
    step.setType(StepType.TASK);
    step.setElementId("Activity_0dpkze6");
    step.setTemplateVariables("{\"lastTask\":\"[( ${templateUtils.now()} )]\"}");
    step.setShouldComplete(true);
    step.setTimeBeforeCompletion(when);
    return step;
  }

  public Step buildStartStep1(long when) {
    Step step = new Step();
    step.setType(StepType.START);
    step.setTemplateVariables(
        "{\"instance\": "
            + (scenarioIdx++)
            + ", \"input\": \"auto\", \"firstname\":\"[( ${templateUtils.firstname()} )]\"}");
    step.setShouldComplete(true);
    step.setTimeBeforeCompletion(when);
    return step;
  }

  public Step buildStartStep2(long when) {
    Step step = new Step();
    step.setType(StepType.START);
    step.setTemplateVariables(
        "{\"instance\": "
            + (scenarioIdx++)
            + ", \"input\": \"manuel\", \"firstname\":\"[( ${templateUtils.firstname()} )]\"}");
    step.setShouldComplete(true);
    step.setTimeBeforeCompletion(when);
    return step;
  }
}
