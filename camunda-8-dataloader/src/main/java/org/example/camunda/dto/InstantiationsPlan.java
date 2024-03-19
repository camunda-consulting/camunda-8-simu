package org.example.camunda.dto;

public class InstantiationsPlan {

  private Integer daysDuration;
  private Long instancesFirstDay;
  private Long instancesLastDay;
  private String evolution;
  private Integer chainsawFrom;
  private Integer chainsawTo;

  public Integer getDaysDuration() {
    return daysDuration;
  }

  public void setDaysDuration(Integer daysDuration) {
    this.daysDuration = daysDuration;
  }

  public Long getInstancesFirstDay() {
    return instancesFirstDay;
  }

  public void setInstancesFirstDay(Long instancesFirstDay) {
    this.instancesFirstDay = instancesFirstDay;
  }

  public Long getInstancesLastDay() {
    return instancesLastDay;
  }

  public void setInstancesLastDay(Long instancesLastDay) {
    this.instancesLastDay = instancesLastDay;
  }

  public String getEvolution() {
    return evolution;
  }

  public void setEvolution(String evolution) {
    this.evolution = evolution;
  }

  public Integer getChainsawFrom() {
    return chainsawFrom;
  }

  public void setChainsawFrom(Integer chainsawFrom) {
    this.chainsawFrom = chainsawFrom;
  }

  public Integer getChainsawTo() {
    return chainsawTo;
  }

  public void setChainsawTo(Integer chainsawTo) {
    this.chainsawTo = chainsawTo;
  }
}
