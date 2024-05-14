package org.example.camunda.dto;

public class PostStepAction {
  private StepActionEnum action = StepActionEnum.CLOCK;

  // CLOCK
  private Long timeAdvance = 1000L;

  // MSG
  private String msg = "MSG";
  private String correlationId;
  private Long delayBeforeMsg = 0L;
  private String jsonTemplate = "{}";
}
