package org.example.camunda.core;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class StartPiScheduler {

  @Autowired private ThreadPoolTaskScheduler taskScheduler;
  @Autowired private ZeebeService processService;
  private ScheduledFuture<?> scheduledPi;

  public void start() {
    scheduledPi =
        taskScheduler.scheduleWithFixedDelay(
            () -> {
              processService.startProcessInstance("", 1, "");
            },
            Duration.ofSeconds(1));
  }

  public void stop() {
    scheduledPi.cancel(true);
  }
}
