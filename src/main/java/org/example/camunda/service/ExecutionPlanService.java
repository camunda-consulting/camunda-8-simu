package org.example.camunda.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.example.camunda.dto.ExecutionPlan;
import org.example.camunda.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExecutionPlanService {

  public static final String REPO = "plans";

  @Value("${workspace:workspace}")
  private String workspace;

  public Path resolve(String bpmnProcessId) {
    return Path.of(workspace).resolve(REPO).resolve(bpmnProcessId);
  }

  public List<String> list() {
    return Stream.of(Path.of(workspace).resolve(REPO).toFile().listFiles())
        .map(File::getName)
        .collect(Collectors.toList());
  }

  public ExecutionPlan find(String bpmnProcessId) throws IOException {
    Path planPath = resolve(bpmnProcessId);
    if (!planPath.toFile().exists()) {
      return null;
    }
    return JsonUtils.fromJsonFile(planPath, ExecutionPlan.class);
  }

  public ExecutionPlan save(ExecutionPlan plan) throws IOException {
    JsonUtils.toJsonFile(
        resolve(plan.getDefinition().getBpmnProcessId()), plan);
    return plan;
  }

  public void delete(String bpmnProcessId) throws IOException {
    Files.delete(resolve(bpmnProcessId));
  }
}
