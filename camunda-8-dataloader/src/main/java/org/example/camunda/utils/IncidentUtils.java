package org.example.camunda.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.example.camunda.dto.IncidentTypeEnum;

public class IncidentUtils {

  public static String error(IncidentTypeEnum incident) {
    if (incident == IncidentTypeEnum.NULLPOINTER) {
      return nullPointer();
    }
    if (incident == IncidentTypeEnum.ARITHMETIC) {
      return arithmetic();
    }
    if (incident == IncidentTypeEnum.OUT_OF_BOUND) {
      return outOfBound();
    }
    if (incident == IncidentTypeEnum.ILLEGAL_ARGUMENT) {
      return illegalArgument();
    }
    if (incident == IncidentTypeEnum.TIME_OUT) {
      return timeout();
    }
    return "Exception occured at line 12";
  }

  public static String nullPointer() {
    try {
      String x = null;
      return x.substring(12);
    } catch (NullPointerException e) {
      return ExceptionUtils.getStackTrace(e);
    }
  }

  public static String arithmetic() {
    try {
      int x = 12 / 0;
      return "Magic maths";
    } catch (Exception e) {
      return ExceptionUtils.getStackTrace(e);
    }
  }

  public static String outOfBound() {
    try {
      List<String> x = new ArrayList<>();
      return x.get(-1);
    } catch (IndexOutOfBoundsException e) {
      return ExceptionUtils.getStackTrace(e);
    }
  }

  public static String illegalArgument() {
    return ExceptionUtils.getStackTrace(
        new IllegalArgumentException("Attribute must be greater than zero"));
  }

  public static String timeout() {
    return ExceptionUtils.getStackTrace(
        new TimeoutException("deadline exceeded after 19.998259400s"));
  }
}
