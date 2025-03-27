package org.example.camunda.conf;

public class TasklistConf {
  private String url = null;
  private String audience = "tasklist.camunda.io";

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getAudience() {
    return audience;
  }

  public void setAudience(String audience) {
    this.audience = audience;
  }
}
