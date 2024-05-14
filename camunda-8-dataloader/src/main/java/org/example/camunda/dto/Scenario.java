package org.example.camunda.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.Map;

public class Scenario {
  private String name;
  @JsonIgnore private String bpmnProcessId;
  @JsonIgnore private Long version = -1L;
  @JsonIgnore private PlanPrecisionEnum precision;
  private LocalDate firstDay;
  private LocalDate lastDay;
  private Integer nbInstancesStart;
  private Integer nbInstancesEnd;
  private ScenarioProgressionEnum evolution;
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

  public PlanPrecisionEnum getPrecision() {
    return precision;
  }

  public void setPrecision(PlanPrecisionEnum precision) {
    this.precision = precision;
  }

  public LocalDate getFirstDay() {
    return firstDay;
  }

  public void setFirstDay(LocalDate firstDay) {
    this.firstDay = firstDay;
  }

  public LocalDate getLastDay() {
    return lastDay;
  }

  public void setLastDay(LocalDate lastDay) {
    this.lastDay = lastDay;
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

  public ScenarioProgressionEnum getEvolution() {
    return evolution;
  }

  public void setEvolution(ScenarioProgressionEnum evolution) {
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
