package org.example.camunda.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.example.camunda.dto.progression.Evolution;
import org.example.camunda.dto.templating.JsonTemplate;

@Getter @Setter
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
}
