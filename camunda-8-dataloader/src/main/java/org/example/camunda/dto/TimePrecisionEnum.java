package org.example.camunda.dto;

public enum TimePrecisionEnum {
  SECOND(1000),
  HUNDRED_MILLIS(100),
  TEN_MILLIS(10),
  MILLIS(1);

  TimePrecisionEnum(int millis) {
    this.millis = millis;
  }

  int millis;

  public long round(long time) {
    return (time / millis) * millis;
  }
}
