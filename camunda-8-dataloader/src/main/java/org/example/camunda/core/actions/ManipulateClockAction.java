package org.example.camunda.core.actions;

import org.example.camunda.core.ZeebeService;

public class ManipulateClockAction extends Action {

  private long time;

  public ManipulateClockAction(long time, ZeebeService zeebeService) {
    super();
    this.time = time;
    this.setZeebeService(zeebeService);
  }

  @Override
  public void run() {
    this.getZeebeService().moveClock(time);
  }
}
