package org.example.camunda.utils;

import io.camunda.zeebe.client.api.command.ClientStatusException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import org.example.camunda.dto.HistoPlan;

public class HistoUtils {
  private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

  private static Map<String, HistoPlan> plans = new HashMap<>();

  public static void start() {
    HistoPlan histo = new HistoPlan();
    histo.setBpmnProcessId(ContextUtils.getPlan().getDefinition().getBpmnProcessId());
    plans.put(ContextUtils.getPlan().getName(), histo);
  }

  public static void startInstances(long count) {
    getPlan().getInstancesCreated().addAndGet(count);
  }

  public static void completeInstances(long count) {
    getPlan().getInstancesCompleted().addAndGet(count);
  }

  public static void addError(Exception e) {
    if (e instanceof ClientStatusException cse) {
      getPlan().addError(cse.getStatusCode().name());
    } else {
      getPlan().addError(e.getMessage());
    }
  }

  public static Map<String, HistoPlan> getPlans() {
    if (getPlan() != null && getPlan().isRunning() && ContextUtils.getEngineTime() > 0) {
      getPlan().setCurrentDate(dateFormat.format(new Date(ContextUtils.getEngineTime())));
    }
    return plans;
  }

  public static HistoPlan getPlan() {
    if (ContextUtils.getPlan() == null) {
      return null;
    }
    return getPlan(ContextUtils.getPlan().getName());
  }

  public static HistoPlan getPlan(String name) {
    return plans.get(name);
  }

  public static void stop() {
    getPlan().setRunning(false);
    if (ContextUtils.getEngineTime() > 0) {
      getPlan().setCurrentDate(dateFormat.format(new Date(ContextUtils.getEngineTime())));
    }
  }
}
