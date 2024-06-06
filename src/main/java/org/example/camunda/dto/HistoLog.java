package org.example.camunda.dto;

public class HistoLog {

  private String planCode;
  private String bpmnProcessId;
  private String log;
  private long realDate;
  private long engineDate;

  public String getPlanCode() {
    return planCode;
  }

  public void setPlanCode(String planCode) {
    this.planCode = planCode;
  }

  public String getBpmnProcessId() {
    return bpmnProcessId;
  }

  public void setBpmnProcessId(String bpmnProcessId) {
    this.bpmnProcessId = bpmnProcessId;
  }

  public String getLog() {
    return log;
  }

  public void setLog(String log) {
    this.log = log;
  }

  public long getRealDate() {
    return realDate;
  }

  public void setRealDate(long realDate) {
    this.realDate = realDate;
  }

  public long getEngineDate() {
    return engineDate;
  }

  public void setEngineDate(long engineDate) {
    this.engineDate = engineDate;
  }
}
