package org.example.camunda.facade;

import java.io.IOException;
import java.util.Collection;
import org.example.camunda.core.PayloadGenerator;
import org.example.camunda.dto.templating.Dataset;
import org.example.camunda.dto.templating.JsonTemplate;
import org.example.camunda.dto.templating.TemplatingMethod;
import org.example.camunda.utils.DataUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/templating")
public class TemplatingController {

  @GetMapping
  public Collection<String> getTemplatingMethodNames() {
    return PayloadGenerator.getTemplatingMethods().keySet();
  }

  @GetMapping("method/{methodName}")
  public Collection<TemplatingMethod> getTemplatingMethods(@PathVariable String methodName) {
    return PayloadGenerator.getTemplatingMethods().get(methodName);
  }

  @PostMapping("test")
  public String evaluateTemplate(@RequestBody JsonTemplate jsonTemplate) {
    return PayloadGenerator.generatePayload(
        jsonTemplate.getTemplate(), jsonTemplate.getExampleContext());
  }

  @GetMapping("datasets")
  public Collection<String> getDatasets() {
    return DataUtils.getDatasets().keySet();
  }

  @GetMapping("dataset/{name}")
  public Dataset getDataset(@PathVariable String name) {
    return DataUtils.getDataset(name);
  }

  @PostMapping("datasets")
  public void saveDataset(@RequestBody Dataset dataset) throws IOException {
    DataUtils.saveDataset(dataset);
  }

  @DeleteMapping("dataset/{name}")
  public void deleteDataset(@PathVariable String name) throws IOException {
    DataUtils.deleteDataset(name);
  }
}
