package org.example.camunda.conf;

public class ZeebeConf {
  public String grpc = null;

  public String rest = null;

  public String audience = "zeebe.camunda.io";

  private String deploy = null;

  public boolean plaintext = false;

  public String getGrpc() {
    return grpc;
  }

  public void setGrpc(String grpc) {
    this.grpc = grpc;
  }

  public String getRest() {
    return rest;
  }

  public void setRest(String rest) {
    this.rest = rest;
  }

  public String getAudience() {
    return audience;
  }

  public void setAudience(String audience) {
    this.audience = audience;
  }

  public String getDeploy() {
    return deploy;
  }

  public void setDeploy(String deploy) {
    this.deploy = deploy;
  }

  public boolean isPlaintext() {
    return plaintext;
  }

  public void setPlaintext(boolean plaintext) {
    this.plaintext = plaintext;
  }
}
