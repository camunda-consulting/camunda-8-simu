package org.example.camunda.conf;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "client")
public class ClientConfiguration {

  public String consultantMail = null;

  public String clientId = null;

  public String clientSecret = null;

  public String clusterId = null;

  public String region = null;

  public String oauthUrl = "https://login.cloud.camunda.io/oauth/token";

  /*public OperateConf operate = new OperateConf();

    public TasklistConf tasklist = new TasklistConf();

    public OptimizeConf optimize = new OptimizeConf();
  */
  public ZeebeConf zeebe;

  public URI getZeebeRest() {
    try {
      if (getZeebe().getRest() != null) {
        return new URI(zeebe.getRest());
      }
      if (clusterId != null && region != null) {
        return new URI("https://" + region + ".zeebe.camunda.io/" + clusterId);
      }
    } catch (URISyntaxException e) {

    }
    throw new IllegalArgumentException(
        "please provide zeebe.client.zeebeRest or zeebe.client.cloud.clusterId&region");
  }

  public URI getZeebeGrpc() {
    try {
      if (getZeebe().getGrpc() != null) {
        return new URI(zeebe.getGrpc());
      }
      if (clusterId != null && region != null) {
        return new URI("grpcs://" + clusterId + "." + region + ".zeebe.camunda.io:443");
      }
    } catch (URISyntaxException e) {

    }

    throw new IllegalArgumentException(
        "please provide zeebe.client.zeebeGrpc or zeebe.client.cloud.clusterId&region");
  }

  public OAuthCredentialsProvider credentialsProvider() {

    return new OAuthCredentialsProviderBuilder()
        .authorizationServerUrl(oauthUrl)
        .audience(getZeebe().getAudience())
        .clientId(clientId)
        .clientSecret(clientSecret)
        .build();
  }

  public ZeebeClient getZeebeClient() {
    ZeebeClientBuilder builder =
        ZeebeClient.newClientBuilder()
            .grpcAddress(getZeebeGrpc())
            .restAddress(getZeebeRest())
            .numJobWorkerExecutionThreads(50)
            .defaultJobWorkerStreamEnabled(true)
            .defaultJobWorkerMaxJobsActive(100);
    if (clientId != null && clientSecret != null) {
      builder.credentialsProvider(credentialsProvider());
    }
    if (zeebe.isPlaintext()) {
      builder.usePlaintext();
    }
    return builder.build();
  }

  /*public String getOperateUrl() {
    if (operate.getUrl() != null) {
      return operate.getUrl();
    }
    return "https://" + region + ".operate.camunda.io/" + clusterId;
  }

  public String getTasklistUrl() {
    if (tasklist.getUrl() != null) {
      return tasklist.getUrl();
    }
    return "https://" + region + ".tasklist.camunda.io/" + clusterId;
  }

  public String getOptimize() {
    if (optimize.getUrl() != null) {
      return optimize.getUrl();
    }
    return "https://" + region + ".optimize.camunda.io/" + clusterId;
  }*/
  /*
  public CamundaOperateClient getOperateClient() throws OperateException {
    Authentication auth = null;
    if (clusterId != null) {
      JwtConfig jwtConfig = new JwtConfig();
      jwtConfig.addProduct(
          Product.OPERATE,
          new JwtCredential(clientId, clientSecret, operate.getAudience(), oauthUrl));
      // targetOperateUrl = "https://" + region + ".operate.camunda.io/" + clusterId;
      auth = SaaSAuthentication.builder().jwtConfig(jwtConfig).build();

    } else {
      JwtConfig jwtConfig = new JwtConfig();
      jwtConfig.addProduct(Product.OPERATE, new JwtCredential(clientId, clientSecret, null, null));
      auth = SelfManagedAuthentication.builder().jwtConfig(jwtConfig).keycloakUrl(oauthUrl).build();
    }
    CamundaOperateClient client =
        CamundaOperateClient.builder()
            .operateUrl(getOperateUrl())
            .authentication(auth)
            .setup()
            .build();

    return client;
  }*/

  public String getConsultantMail() {
    return consultantMail;
  }

  public void setConsultantMail(String consultantMail) {
    this.consultantMail = consultantMail;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public void setClusterId(String clusterId) {
    this.clusterId = clusterId;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public void setOauthUrl(String oauthUrl) {
    this.oauthUrl = oauthUrl;
  }

  /*public void setTasklist(TasklistConf tasklist) {
    this.tasklist = tasklist;
  }

  public void setOptimize(OptimizeConf optimize) {
    this.optimize = optimize;
  }

  public void setOperate(OperateConf operate) {
    this.operate = operate;
  }*/

  public void setZeebe(ZeebeConf zeebe) {
    this.zeebe = zeebe;
  }

  public ZeebeConf getZeebe() {
    if (this.zeebe == null) {
      this.zeebe = new ZeebeConf();
    }
    return this.zeebe;
  }
}
