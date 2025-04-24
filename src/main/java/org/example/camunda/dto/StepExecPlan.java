package org.example.camunda.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.example.camunda.dto.templating.JsonTemplate;

@Setter @Getter
public class StepExecPlan {
  private String elementId;
  private StepActionEnum action = StepActionEnum.COMPLETE;

  // COMPLETE AND INCIDENT
  private StepDuration duration;

  // COMPLETE
  private JsonTemplate jsonTemplate;
  private List<StepAdditionalAction> postSteps;

  // ANY
  private List<StepAdditionalAction> preSteps;

  // INCIDENT
  private IncidentTypeEnum incident;

}
