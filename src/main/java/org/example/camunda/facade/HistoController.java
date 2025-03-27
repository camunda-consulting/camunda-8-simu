package org.example.camunda.facade;

import java.util.Map;
import org.example.camunda.dto.HistoPlan;
import org.example.camunda.utils.HistoUtils;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/histo")
public class HistoController {

  @GetMapping
  public Map<String, HistoPlan> all() {
    return HistoUtils.getPlans();
  }

  @GetMapping("/current")
  public HistoPlan getProgresses() {
    return HistoUtils.getPlan();
  }
}
