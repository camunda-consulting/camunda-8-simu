package org.example.camunda.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.camunda.common.auth.Authentication;
import io.camunda.common.auth.JwtConfig;
import io.camunda.common.auth.JwtCredential;
import io.camunda.common.auth.Product;
import io.camunda.common.auth.SaaSAuthentication;
import io.camunda.common.auth.SelfManagedAuthentication;
import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.exception.OperateException;
import io.camunda.operate.model.FlowNodeInstance;
import io.camunda.operate.model.FlowNodeInstanceState;
import io.camunda.operate.model.ProcessDefinition;
import io.camunda.operate.model.ProcessInstance;
import io.camunda.operate.model.ProcessInstanceState;
import io.camunda.operate.model.SearchResult;
import io.camunda.operate.model.Variable;
import io.camunda.operate.search.FlowNodeInstanceFilter;
import io.camunda.operate.search.ProcessDefinitionFilter;
import io.camunda.operate.search.ProcessInstanceFilter;
import io.camunda.operate.search.SearchQuery;
import io.camunda.operate.search.Sort;
import io.camunda.operate.search.SortOrder;
import io.camunda.operate.search.VariableFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.example.camunda.dto.CustomProcessInstanceFilter;
import org.example.camunda.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

@Service
@EnableCaching
public class OperateService {

  private static final Logger LOG = LoggerFactory.getLogger(OperateService.class);

  @Value("${operate.cloud.clientId:notProvided}")
  private String clientId;

  @Value("${operate.cloud.clientSecret:notProvided}")
  private String clientSecret;

  @Value("${operate.cloud.clusterId:notProvided}")
  private String clusterId;

  @Value("${operate.cloud.region:notProvided}")
  private String region;

  @Value("${operate.selfmanaged.clientId:notProvided}")
  private String identityClientId;

  @Value("${operate.selfmanaged.clientSecret:notProvided}")
  private String identityClientSecret;

  @Value("${operate.selfmanaged.url:notProvided}")
  private String operateUrl;

  @Value("${operate.selfmanaged.keycloakUrl:notProvided}")
  private String keycloakUrl;

  private CamundaOperateClient client;

  private CamundaOperateClient getCamundaOperateClient() throws OperateException {
    if (client == null) {
      String targetOperateUrl = operateUrl;
      Authentication auth = null;
      if (!"notProvided".equals(clientId)) {
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.addProduct(
            Product.OPERATE,
            new JwtCredential(
                clientId,
                clientSecret,
                "operate.camunda.io",
                "https://login.cloud.camunda.io/oauth/token"));
        targetOperateUrl = "https://" + region + ".operate.camunda.io/" + clusterId;
        auth = SaaSAuthentication.builder().jwtConfig(jwtConfig).build();

      } else {
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.addProduct(
            Product.OPERATE, new JwtCredential(identityClientId, identityClientSecret, null, null));
        auth =
            SelfManagedAuthentication.builder()
                .jwtConfig(jwtConfig)
                .keycloakUrl(keycloakUrl)
                .build();
      }
      client =
          CamundaOperateClient.builder()
              .operateUrl(targetOperateUrl)
              .authentication(auth)
              .setup()
              .build();
    }
    return client;
  }

  public List<ProcessDefinition> getProcessDefinitions() throws OperateException {
    ProcessDefinitionFilter processDefinitionFilter = ProcessDefinitionFilter.builder().build();
    SearchQuery procDefQuery =
        new SearchQuery.Builder()
            .filter(processDefinitionFilter)
            .size(1000)
            .sort(new Sort("version", SortOrder.DESC))
            .build();

    return getCamundaOperateClient().searchProcessDefinitions(procDefQuery);
  }

  @Cacheable("processXmls")
  public String getProcessDefinitionXmlByKey(Long key) throws OperateException {
    LOG.info("Entering getProcessDefinitionXmlByKey for key " + key);
    return getCamundaOperateClient().getProcessDefinitionXml(key);
  }

  public Map<String, Set<JsonNode>> listVariables() throws OperateException, IOException {
    List<Variable> vars =
        getCamundaOperateClient()
            .searchVariables(
                new SearchQuery.Builder().filter(new VariableFilter()).size(1000).build());
    Map<String, Set<JsonNode>> result = new HashMap<>();
    for (Variable var : vars) {
      if (!result.containsKey(var.getName())) {
        result.put(var.getName(), new HashSet<>());
      }
      result.get(var.getName()).add(JsonUtils.toJsonNode(var.getValue()));
    }
    return result;
  }

  public List<ProcessInstance> listInstances(ProcessInstanceState state) throws OperateException {
    return getCamundaOperateClient()
        .searchProcessInstances(
            new SearchQuery.Builder()
                .filter(ProcessInstanceFilter.builder().state(state).build())
                .size(100)
                .build());
  }

  public List<FlowNodeInstance> listActiveFlowNodes(Long processInstanceKey)
      throws OperateException {
    return getCamundaOperateClient()
        .searchFlowNodeInstances(
            new SearchQuery.Builder()
                .filter(
                    FlowNodeInstanceFilter.builder()
                        .processInstanceKey(processInstanceKey)
                        .state(FlowNodeInstanceState.ACTIVE)
                        .build())
                .size(100)
                .build());
  }

  public ProcessInstance getInstance(Long processInstanceKey) throws OperateException {
    return getCamundaOperateClient().getProcessInstance(processInstanceKey);
  }

  public Map<String, Object> getVariables(Long processInstanceKey) throws OperateException {
    // TODO Auto-generated method stub
    List<Variable> variables =
        getCamundaOperateClient()
            .searchVariables(
                new SearchQuery.Builder()
                    .filter(VariableFilter.builder().processInstanceKey(processInstanceKey).build())
                    .size(100)
                    .build());
    Map<String, Object> result = new HashMap<>();
    for (Variable var : variables) {
      result.put(var.getName(), JsonUtils.eventuallyJsonNode(var.getValue()));
    }

    return result;
  }

  public ProcessInstance getSubProcessInstance(Long parentInstanceKey, Long parentFlowNodeKey)
      throws OperateException {
    List<ProcessInstance> instances =
        getCamundaOperateClient()
            .searchProcessInstances(
                new SearchQuery.Builder()
                    .filter(new CustomProcessInstanceFilter(parentInstanceKey, parentFlowNodeKey))
                    .size(1)
                    .build());
    if (instances.isEmpty()) {
      return null;
    }
    return instances.get(0);
  }

  public ProcessDefinition getProcessDefinition(String bpmnProcessId, Long version)
      throws OperateException {
    ProcessDefinitionFilter processDefinitionFilter =
        ProcessDefinitionFilter.builder().bpmnProcessId(bpmnProcessId).version(version).build();
    SearchQuery procDefQuery =
        new SearchQuery.Builder().filter(processDefinitionFilter).size(1).build();

    List<ProcessDefinition> defs = getCamundaOperateClient().searchProcessDefinitions(procDefQuery);
    if (defs.isEmpty()) {
      return null;
    }
    return defs.get(0);
  }

  public SearchResult<FlowNodeInstance> getIntermediateCatchEvents(List<Object> searchAfter)
      throws OperateException {
    return getCamundaOperateClient()
        .searchFlowNodeInstanceResults(
            new SearchQuery.Builder()
                .filter(
                    FlowNodeInstanceFilter.builder()
                        .type("INTERMEDIATE_CATCH_EVENT")
                        .state(FlowNodeInstanceState.ACTIVE)
                        .build())
                .size(30)
                .searchAfter(searchAfter)
                .build());
  }
}
