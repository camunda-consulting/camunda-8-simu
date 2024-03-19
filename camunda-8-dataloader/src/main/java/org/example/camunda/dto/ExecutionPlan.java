package org.example.camunda.dto;

import io.camunda.operate.model.ProcessDefinition;

public class ExecutionPlan {
  private ProcessDefinition definition;
  private String xml;
  private Boolean xmlModified = false;
  private InstantiationsPlan instantiationsPlan;

  public ProcessDefinition getDefinition() {
    return definition;
  }

  public void setDefinition(ProcessDefinition definition) {
    this.definition = definition;
  }

  public String getXml() {
    return xml;
  }

  public void setXml(String xml) {
    this.xml = xml;
  }

  public Boolean getXmlModified() {
    return xmlModified;
  }

  public void setXmlModified(Boolean xmlModified) {
    this.xmlModified = xmlModified;
  }

  public InstantiationsPlan getInstantiationsPlan() {
    return instantiationsPlan;
  }

  public void setInstantiationsPlan(InstantiationsPlan instantiationsPlan) {
    this.instantiationsPlan = instantiationsPlan;
  }
}
