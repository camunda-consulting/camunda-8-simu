package org.example.camunda.core.actions;

import java.util.ArrayList;
import java.util.List;
import org.example.camunda.utils.ContextUtils;

public class ClockAction extends Action {

  private final List<String> processUniqueIds;
  private final long time;

  public ClockAction(String processUniqueId, long time) {
    super();
    this.processUniqueIds = new ArrayList<>();
    this.processUniqueIds.add(processUniqueId);
    this.time = time;
  }

  @Override
  public void run() {
    for (String processUniqueId : processUniqueIds) {
      ContextUtils.setProcessInstanceTime(processUniqueId, time);
    }
  }

  @Override
  public String getType() {
    return "Clock";
  }

  public void addUniqueId(String uniqueId) {
    this.processUniqueIds.add(uniqueId);
  }
}
