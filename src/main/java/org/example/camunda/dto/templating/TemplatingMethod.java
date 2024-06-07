package org.example.camunda.dto.templating;

import java.util.List;

public class TemplatingMethod {

  private String name;
  private String resultType;
  private List<MethodArg> args;
  private String example;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getResultType() {
    return resultType;
  }

  public void setResultType(String resultType) {
    this.resultType = resultType;
  }

  public List<MethodArg> getArgs() {
    return args;
  }

  public void setArgs(List<MethodArg> args) {
    this.args = args;
  }

  public String getExample() {
    return example;
  }

  public void setExample(String example) {
    this.example = example;
  }
}
