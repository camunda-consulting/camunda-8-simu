package org.example.camunda.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class Scenario {
  private String name;
  @JsonIgnore private String bpmnProcessId;
  @JsonIgnore private Long version = -1L;
  @JsonIgnore private ChronoUnit instanceDistribution;
  @JsonIgnore private TimePrecisionEnum timePrecision = TimePrecisionEnum.HUNDRED_MILLIS;
  private int dayTimeStart = 9;
  private int dayTimeEnd = 18;
  private String firstDayFeelExpression;
  private String lastDayFeelExpression;
  private Integer nbInstancesStart;
  private Integer nbInstancesEnd;
  private ProgressionEnum evolution;
  private Integer saltMin;
  private Integer saltMax;
  private Map<String, StepExecPlan> steps;
  // start template
  private String jsonTemplate = "{}";

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getBpmnProcessId() {
    return bpmnProcessId;
  }

  public void setBpmnProcessId(String bpmnProcessId) {
    this.bpmnProcessId = bpmnProcessId;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public ChronoUnit getInstanceDistribution() {
    return instanceDistribution;
  }

  public void setInstanceDistribution(ChronoUnit instanceDistribution) {
    this.instanceDistribution = instanceDistribution;
  }

  public TimePrecisionEnum getTimePrecision() {
    return timePrecision;
  }

  public void setTimePrecision(TimePrecisionEnum timePrecision) {
    this.timePrecision = timePrecision;
  }

  public int getDayTimeStart() {
    return dayTimeStart;
  }

  public void setDayTimeStart(int dayTimeStart) {
    this.dayTimeStart = dayTimeStart;
  }

  public int getDayTimeEnd() {
    return dayTimeEnd;
  }

  public void setDayTimeEnd(int dayTimeEnd) {
    this.dayTimeEnd = dayTimeEnd;
  }

  public String getFirstDayFeelExpression() {
    return firstDayFeelExpression;
  }

  public void setFirstDayFeelExpression(String firstDayFeelExpression) {
    this.firstDayFeelExpression = firstDayFeelExpression;
  }

  public String getLastDayFeelExpression() {
    return lastDayFeelExpression;
  }

  public void setLastDayFeelExpression(String lastDayFeelExpression) {
    this.lastDayFeelExpression = lastDayFeelExpression;
  }

  public Integer getNbInstancesStart() {
    return nbInstancesStart;
  }

  public void setNbInstancesStart(Integer nbInstancesStart) {
    this.nbInstancesStart = nbInstancesStart;
  }

  public Integer getNbInstancesEnd() {
    return nbInstancesEnd;
  }

  public void setNbInstancesEnd(Integer nbInstancesEnd) {
    this.nbInstancesEnd = nbInstancesEnd;
  }

  public ProgressionEnum getEvolution() {
    return evolution;
  }

  public void setEvolution(ProgressionEnum evolution) {
    this.evolution = evolution;
  }

  public Integer getSaltMin() {
    return saltMin;
  }

  public void setSaltMin(Integer saltMin) {
    this.saltMin = saltMin;
  }

  public Integer getSaltMax() {
    return saltMax;
  }

  public void setSaltMax(Integer saltMax) {
    this.saltMax = saltMax;
  }

  public Map<String, StepExecPlan> getSteps() {
    return steps;
  }

  public void setSteps(Map<String, StepExecPlan> steps) {
    this.steps = steps;
  }

  public String getJsonTemplate() {
    return jsonTemplate;
  }

  public void setJsonTemplate(String jsonTemplate) {
    this.jsonTemplate = jsonTemplate;
  }
}
