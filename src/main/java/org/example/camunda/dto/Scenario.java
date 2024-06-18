package org.example.camunda.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import org.example.camunda.dto.progression.Evolution;
import org.example.camunda.dto.templating.JsonTemplate;

public class Scenario {
  private String name;
  @JsonIgnore private String bpmnProcessId;
  @JsonIgnore private Long version = -1L;
  private int dayTimeStart = 9;
  private int dayTimeEnd = 18;
  private String firstDayFeelExpression;
  private String lastDayFeelExpression;
  private Evolution evolution;
  private InstanceStartTypeEnum startType;
  // if start type is msg
  private String startMsgName;
  private Map<String, StepExecPlan> steps;
  // start template
  private JsonTemplate jsonTemplate;

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

  public Evolution getEvolution() {
    return evolution;
  }

  public void setEvolution(Evolution evolution) {
    this.evolution = evolution;
  }

  public InstanceStartTypeEnum getStartType() {
    return startType;
  }

  public void setStartType(InstanceStartTypeEnum startType) {
    this.startType = startType;
  }

  public String getStartMsgName() {
    return startMsgName;
  }

  public void setStartMsgName(String startMsgName) {
    this.startMsgName = startMsgName;
  }

  public Map<String, StepExecPlan> getSteps() {
    return steps;
  }

  public void setSteps(Map<String, StepExecPlan> steps) {
    this.steps = steps;
  }

  public JsonTemplate getJsonTemplate() {
    return jsonTemplate;
  }

  public void setJsonTemplate(JsonTemplate jsonTemplate) {
    this.jsonTemplate = jsonTemplate;
  }
}
