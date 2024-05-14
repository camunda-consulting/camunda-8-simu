package org.example.camunda.worker;

import org.springframework.stereotype.Component;

@Component
public class MigrationWorker {
  /*
  @Autowired private ScenarioExecutor scenarioExecutor;

  @JobWorker(autoComplete = false)
  public void auto(ActivatedJob job, @VariablesAsType Map<String, Object> variables)
      throws IOException {
    this.scenarioExecutor.executeWorker(job, variables);
  }

  @JobWorker(autoComplete = false)
  public void lastTask(ActivatedJob job, @VariablesAsType Map<String, Object> variables)
      throws IOException {
    this.scenarioExecutor.executeWorker(job, variables);
  }

  @JobWorker(type = "io.camunda.zeebe:userTask", autoComplete = false) // set timeout to 30 days
  public void listenUserTask(final ActivatedJob job, @VariablesAsType Map<String, Object> variables)
      throws IOException {
    this.scenarioExecutor.executeWorker(job, variables);
  }
  */
}
