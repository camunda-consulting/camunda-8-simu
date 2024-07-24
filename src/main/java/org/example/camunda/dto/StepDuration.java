package org.example.camunda.dto;

public class StepDuration {
  private String startDesiredAvg = "PT8S";
  private String endDesiredAvg = "PT4S";
  private int minMaxPercent = 20;
  private ProgressionEnum avgProgression = ProgressionEnum.LINEAR;
  private long progressionSalt = 1000;

  public String getStartDesiredAvg() {
    return startDesiredAvg;
  }

  public void setStartDesiredAvg(String startDesiredAvg) {
    this.startDesiredAvg = startDesiredAvg;
  }

  public String getEndDesiredAvg() {
    return endDesiredAvg;
  }

  public void setEndDesiredAvg(String endDesiredAvg) {
    this.endDesiredAvg = endDesiredAvg;
  }

  public int getMinMaxPercent() {
    return minMaxPercent;
  }

  public void setMinMaxPercent(int minMaxPercent) {
    this.minMaxPercent = minMaxPercent;
  }

  public ProgressionEnum getAvgProgression() {
    return avgProgression;
  }

  public void setAvgProgression(ProgressionEnum avgProgression) {
    this.avgProgression = avgProgression;
  }

  public long getProgressionSalt() {
    return progressionSalt;
  }

  public void setProgressionSalt(long progressionSalt) {
    this.progressionSalt = progressionSalt;
  }
}
