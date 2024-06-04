package org.example.camunda.facade;

import java.util.Map;
import org.example.camunda.dto.ExecutionPlan;
import org.example.camunda.dto.HistoLog;
import org.example.camunda.dto.SearchResult;
import org.example.camunda.utils.HistoUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  @PostMapping("/search")
  public SearchResult<HistoLog> search(
      @RequestBody String request,
      @RequestParam(required = false) String before,
      @RequestParam(required = false) String after,
      @RequestParam(required = false) Integer size) {
    return HistoUtils.search(request, before, after, size);
  }
}
