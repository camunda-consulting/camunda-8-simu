package org.example.camunda.dto.progression;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @Type(value = ExponentialEvolution.class, name = "exponential"),
  @Type(value = LinearEvolution.class, name = "linear"),
  @Type(value = SaltedLinearEvolution.class, name = "saltedlinear"),
  @Type(value = NormalEvolution.class, name = "normal")
})
public class Evolution {

  private Integer min;
  private Integer max;

  public Integer getMin() {
    return min;
  }

  public void setMin(Integer min) {
    this.min = min;
  }

  public Integer getMax() {
    return max;
  }

  public void setMax(Integer max) {
    this.max = max;
  }
}
