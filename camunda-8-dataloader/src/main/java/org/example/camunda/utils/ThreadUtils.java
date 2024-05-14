package org.example.camunda.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtils {
  private static final Logger LOG = LoggerFactory.getLogger(ThreadUtils.class);

  public static void pause(long timeMillis) {

    try {
      Thread.sleep(timeMillis);
    } catch (InterruptedException e) {
      LOG.error("Error pausing thread", e);
    }
  }
}
