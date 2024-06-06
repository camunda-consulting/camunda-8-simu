package org.example.camunda.core;

import java.util.Map;
import org.example.camunda.utils.TemplatingUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

public class PayloadGenerator {
  private static TemplatingUtils tuInstance = new TemplatingUtils();
  private static TemplateEngine templateEngine;

  private PayloadGenerator() {}

  public static void configure() {}

  public static TemplateEngine getTemplateEngine() {
    if (templateEngine == null) {
      templateEngine = new SpringTemplateEngine();
      StringTemplateResolver templateResolver = new StringTemplateResolver();
      templateResolver.setTemplateMode(TemplateMode.TEXT);
      templateEngine.setTemplateResolver(templateResolver);
    }
    return templateEngine;
  }

  public static String generatePayload(String template, Map<String, Object> variables) {
    Context context = new Context();
    context.setVariable("templateUtils", tuInstance);
    if (variables != null) {
      for (Map.Entry<String, Object> entry : variables.entrySet()) {
        context.setVariable(entry.getKey(), entry.getValue());
      }
    }
    // context.setVariable("firstname", "toto");
    // template = "{\"instance\": 0, \"input\": \"manuel\", \"firstname\":\"[th(${firstname})]\"}";
    return getTemplateEngine().process(template, context);
  }
}
