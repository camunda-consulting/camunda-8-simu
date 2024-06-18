package org.example.camunda.code;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.example.camunda.core.PayloadGenerator;
import org.junit.jupiter.api.Test;

public class PayloadGeneratorTest {

  @Test
  public void generatePayload() {
    String template = "{\"attr\":templateUtils.sequentialNumber(),\"attr2\":\"[(${firstname})]\"}";
    String result = PayloadGenerator.generatePayload(template, Map.of("firstname", "michel"));
    assertEquals("{\"attr\":0,\"attr2\":\"michel\"}", result);

    template = "[(${firstname})]";
    result = PayloadGenerator.generatePayload(template, Map.of("firstname", "michel"));
    assertEquals("michel", result);

    template = "bob";
    result = PayloadGenerator.generatePayload(template, Map.of("firstname", "michel"));
    assertEquals("bob", result);
  }
}
