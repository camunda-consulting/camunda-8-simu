package org.example.camunda.facade;

import java.util.Collection;
import org.example.camunda.core.PayloadGenerator;
import org.example.camunda.dto.templating.JsonTemplate;
import org.example.camunda.dto.templating.TemplatingMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/templating")
public class TemplatingController {

  @GetMapping
  public Collection<TemplatingMethod> getTemplatingMethods() {
    return PayloadGenerator.getTemplatingMethods().values();
  }

  @PostMapping("test")
  public String evaluateTemplate(@RequestBody JsonTemplate jsonTemplate) {
    return PayloadGenerator.generatePayload(
        jsonTemplate.getTemplate(), jsonTemplate.getExampleContext());
  }
}
