package org.example.camunda.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class HistoPlan {

  private String bpmnProcessId;
  private String currentDate;
  private final AtomicLong instancesCreated = new AtomicLong();
  private final AtomicLong instancesCompleted = new AtomicLong();
  private final Map<String, Long> errors = new HashMap<>();
  private boolean running = true;

  public String getBpmnProcessId() {
    return bpmnProcessId;
  }

  public void setBpmnProcessId(String bpmnProcessId) {
    this.bpmnProcessId = bpmnProcessId;
  }

  public String getCurrentDate() {
    return currentDate;
  }

  public void setCurrentDate(String currentDate) {
    this.currentDate = currentDate;
  }

  public AtomicLong getInstancesCreated() {
    return instancesCreated;
  }

  public AtomicLong getInstancesCompleted() {
    return instancesCompleted;
  }

  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean running) {
    this.running = running;
  }

  public synchronized void addError(String error) {
    if (errors.containsKey(error)) {
      errors.put(error, errors.get(error) + 1);
    } else {
      errors.put(error, 1L);
    }
  }

  public Map<String, Long> getErrors() {
    return errors;
  }
}
