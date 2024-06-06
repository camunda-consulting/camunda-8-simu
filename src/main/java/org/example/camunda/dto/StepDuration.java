package org.example.camunda.dto;

public class StepDuration {
  private long startDesiredAvg = 8000;
  private long endDesiredAvg = 4000;
  private int minMaxPercent = 20;
  private ProgressionEnum avgProgression = ProgressionEnum.LINEAR;
  private long progressionSalt = 1000;

  public long getStartDesiredAvg() {
    return startDesiredAvg;
  }

  public void setStartDesiredAvg(long startDesiredAvg) {
    this.startDesiredAvg = startDesiredAvg;
  }

  public long getEndDesiredAvg() {
    return endDesiredAvg;
  }

  public void setEndDesiredAvg(long endDesiredAvg) {
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
