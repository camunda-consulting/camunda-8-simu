package org.example.camunda.facade;

import java.util.List;
import java.util.Map;
import org.example.camunda.dto.ExecutionPlan;
import org.example.camunda.utils.HistoUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/histo")
public class HistoController {

  @GetMapping
  public Map<String, ExecutionPlan> all() {
    return HistoUtils.getPlans();
  }

  @GetMapping("/progresses")
  public Map<String, Integer> getProgresses() {
    return HistoUtils.getProgresses();
  }

  @GetMapping("/logs/{plan}")
  public List<String> histo(@PathVariable String plan) {
    return HistoUtils.getHisto(plan);
  }
}
