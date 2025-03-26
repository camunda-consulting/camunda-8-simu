package org.example.camunda.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.example.camunda.core.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtils {
  private static final Logger LOG = LoggerFactory.getLogger(ThreadUtils.class);

  public static void pause(long timeMillis) {

    try {
      Thread.sleep(timeMillis);
      // LOG.warn("PAUSED " + timeMillis);
    } catch (InterruptedException e) {
      LOG.error("Error pausing thread", e);
    }
  }

  public static void execute(List<Action> actions, boolean parallel) {
    if (parallel) {
      executeInParallel(actions);
    } else {
      for (Action a : actions) {
        a.run();
      }
    }
  }

  public static void executeInParallel(List<Action> actions) {

    try {
      List<CompletableFuture<Boolean>> futures = new ArrayList<>();

      for (Action a : actions) {
        futures.add(
            CompletableFuture.supplyAsync(
                () -> {
                  a.run();
                  return true;
                }));
      }
      CompletableFuture<?>[] futureArray = futures.toArray(new CompletableFuture<?>[0]);
      CompletableFuture<Void> combined = CompletableFuture.allOf(futureArray);

      combined.get();

    } catch (ExecutionException | InterruptedException e) {
      LOG.error("Error executing actions", e);
    }
  }
}
