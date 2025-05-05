package org.example.camunda.dto;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.example.camunda.dto.operate.ProcessDefinition;

@Getter @Setter
public class ExecutionPlan {
  private String name;
  private ProcessDefinition definition;
  private String xml;
  private Map<String, String> xmlDependencies;
  private Map<String, String> dmnDependencies;
  private Boolean xmlModified = false;
  private ExecutionEnd executionEnd = ExecutionEnd.NOW;
  private ChronoUnit instanceDistribution = ChronoUnit.DAYS;
  private String durationsType = "FEEL";
  private List<Scenario> scenarii = new ArrayList<>();
  private String stepLabel = "id";

  public String getName() {
    if (name != null) {
      return name;
    } else return getDefinition().getBpmnProcessId();
  }


}
