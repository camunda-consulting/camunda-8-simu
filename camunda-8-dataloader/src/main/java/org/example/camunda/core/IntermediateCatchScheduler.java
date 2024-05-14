package org.example.camunda.core;

import io.camunda.operate.exception.OperateException;
import io.camunda.operate.model.FlowNodeInstance;
import io.camunda.operate.model.SearchResult;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import org.example.camunda.service.OperateService;
import org.example.camunda.service.ScenarioExecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class IntermediateCatchScheduler {

  @Autowired private ThreadPoolTaskScheduler taskScheduler;
  @Autowired private OperateService operateService;

  private ScheduledFuture<?> scheduled;
  private List<Object> searchAfter = null;

  public void start(ScenarioExecutor executor) {
    scheduled =
        taskScheduler.scheduleWithFixedDelay(
            () -> {
              try {
                SearchResult<FlowNodeInstance> catchEventsResult =
                    operateService.getIntermediateCatchEvents(this.searchAfter);
                List<Object> newSearchAfter = catchEventsResult.getSortValues();
                ;
                if (newSearchAfter != null && !newSearchAfter.isEmpty()) {
                  this.searchAfter = newSearchAfter;
                }
                for (FlowNodeInstance instance : catchEventsResult) {
                  executor.executeIntermediateEvent(instance);
                }

              } catch (OperateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            },
            Duration.ofMillis(300));
  }

  public void stop() {
    scheduled.cancel(true);
  }

  public void start(ScenarioExecService scenarioExecService) {
    scheduled =
        taskScheduler.scheduleWithFixedDelay(
            () -> {
              try {
                SearchResult<FlowNodeInstance> catchEventsResult =
                    operateService.getIntermediateCatchEvents(this.searchAfter);
                List<Object> newSearchAfter = catchEventsResult.getSortValues();
                ;
                if (newSearchAfter != null && !newSearchAfter.isEmpty()) {
                  this.searchAfter = newSearchAfter;
                }
                for (FlowNodeInstance instance : catchEventsResult) {
                  scenarioExecService.handleIntermediateEvent(instance);
                }

              } catch (OperateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            },
            Duration.ofMillis(300));
  }
}
