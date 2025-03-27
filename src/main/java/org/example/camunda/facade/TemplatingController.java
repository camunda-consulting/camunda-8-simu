package org.example.camunda.facade;

import java.io.IOException;
import java.util.Collection;
import org.example.camunda.core.PayloadGenerator;
import org.example.camunda.dto.templating.Dataset;
import org.example.camunda.dto.templating.JsonDataset;
import org.example.camunda.dto.templating.JsonTemplate;
import org.example.camunda.dto.templating.TemplatingMethod;
import org.example.camunda.service.DatasetUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    return PayloadGenerator.templatingMethods.keySet();
  }

  @GetMapping("method/{methodName}")
  public Collection<TemplatingMethod> getTemplatingMethods(@PathVariable String methodName) {
    return PayloadGenerator.templatingMethods.get(methodName);
  }

  @PostMapping("test")
  public String evaluateTemplate(@RequestBody JsonTemplate jsonTemplate) {
    return PayloadGenerator.generatePayload(
        jsonTemplate.getTemplate(), jsonTemplate.getExampleContext());
  }

  @GetMapping("datasets")
  public Collection<String> getDatasets() {
    return DatasetUtils.listDatasets();
  }

  @GetMapping("jsondatasets")
  public Collection<String> getJsonDatasets() {
    return DatasetUtils.listJsonDatasets();
  }

  @GetMapping("dataset/{name}")
  public Dataset getDataset(@PathVariable String name) throws IOException {
    return DatasetUtils.getDataset(name);
  }

  @GetMapping("jsondataset/{name}")
  public JsonDataset getJsonDataset(@PathVariable String name) throws IOException {
    return DatasetUtils.getJsonDataset(name);
  }

  @PostMapping("datasets")
  public void saveDataset(@RequestBody Dataset dataset) throws IOException {
    DatasetUtils.saveDataset(dataset);
  }

  @PostMapping("jsondatasets")
  public void saveJsonDataset(@RequestBody JsonDataset dataset) throws IOException {
    DatasetUtils.saveJsonDataset(dataset);
  }

  @DeleteMapping("dataset/{name}")
  public void deleteDataset(@PathVariable String name) throws IOException {
    DatasetUtils.deleteDataset(name);
  }

  @DeleteMapping("jsondataset/{name}")
  public void deleteJsonDataset(@PathVariable String name) throws IOException {
    DatasetUtils.deleteJsonDataset(name);
  }
}
