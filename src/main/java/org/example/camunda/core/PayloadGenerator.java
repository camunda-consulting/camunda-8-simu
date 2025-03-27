package org.example.camunda.core;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.camunda.dto.templating.MethodArg;
import org.example.camunda.dto.templating.TemplatingMethod;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

public class PayloadGenerator {
  private static Map<String, String> preparedTemplates = new HashMap<>();
  public static final String TEMPLATING_PREFIX = "templateUtils.";
  private static TemplateEngine templateEngine = buildTemplateEngine();
  public static final Map<String, List<TemplatingMethod>> templatingMethods =
      getTemplatingMethods();

  private PayloadGenerator() {}

  public static void configure() {}

  public static TemplateEngine buildTemplateEngine() {
    TemplateEngine templateEngine = new SpringTemplateEngine();
    StringTemplateResolver templateResolver = new StringTemplateResolver();
    templateResolver.setTemplateMode(TemplateMode.TEXT);
    templateEngine.setTemplateResolver(templateResolver);
    return templateEngine;
  }

  private static Map<String, List<TemplatingMethod>> getTemplatingMethods() {
    Map<String, List<TemplatingMethod>> templatingMethods = new HashMap<>();
    Method[] methods = TemplatingUtils.class.getMethods();
    for (Method method : methods) {
      if (Modifier.isPublic(method.getModifiers())) {
        TemplatingMethod tm = new TemplatingMethod();
        tm.setName(method.getName());
        if (!templatingMethods.containsKey(tm.getName())) {
          templatingMethods.put(tm.getName(), new ArrayList<>());
        }
        templatingMethods.get(method.getName()).add(tm);
        String example = TEMPLATING_PREFIX + tm.getName() + "(";
        tm.setResultType(method.getReturnType().getName());
        tm.setArgs(new ArrayList<>());
        int i = 0;
        for (Parameter param : method.getParameters()) {
          MethodArg arg = new MethodArg();
          arg.setName(param.getName());
          arg.setType(param.getType().getName());
          tm.getArgs().add(arg);
          if (i > 0) {
            example += ", ";
          }
          if (arg.getType().equals("java.lang.String")) {
            example += "\"" + arg.getName() + "\"";
          } else if (arg.getType().equals("long")
              || arg.getType().equals("java.lang.Long")
              || arg.getType().equals("int")
              || arg.getType().equals("java.lang.Integer")) {
            example += "1 /*" + arg.getName() + "*/";
          } else if (arg.getType().equals("boolean") || arg.getType().equals("java.lang.Boolean")) {
            example += "true /*" + arg.getName() + "*/";
          } else if (arg.getType().equals("double") || arg.getType().equals("java.lang.Double")) {
            example += "1.0 /*" + arg.getName() + "*/";
          } else {
            example += arg.getName() + "(" + arg.getType() + ")";
          }
          i++;
        }
        example += ")";
        tm.setExample(example);
      }
    }
    return templatingMethods;
  }

  private static String prepareVariables(String jsonTemplate) {
    return jsonTemplate.replaceAll("\\$([a-zA-Z0-9]+)", "[(\\${$1})]");
  }

  public static String prepareThymeleafBlocks(String jsonTemplate) {
    if (preparedTemplates.containsKey(jsonTemplate)) {
      return preparedTemplates.get(jsonTemplate);
    }
    String original = jsonTemplate;
    jsonTemplate = prepareVariables(jsonTemplate);

    int tuIndex = jsonTemplate.indexOf(TEMPLATING_PREFIX);
    while (tuIndex > 0) {
      int openingBracket = jsonTemplate.indexOf("(", tuIndex);
      String methodName =
          jsonTemplate.substring(tuIndex + TEMPLATING_PREFIX.length(), openingBracket);
      int closingBracket = findClosingBracket(jsonTemplate, openingBracket);
      String before = jsonTemplate.substring(0, tuIndex);
      String after = jsonTemplate.substring(closingBracket + 1);
      String replace = "[( ${" + jsonTemplate.substring(tuIndex, closingBracket + 1) + "} )]";

      TemplatingMethod tuMethod = templatingMethods.get(methodName).get(0);
      if (tuMethod.getResultType().equals("java.lang.String")) {
        replace = "\"" + replace + "\"";
      }
      jsonTemplate = before + replace + after;
      tuIndex = jsonTemplate.indexOf(TEMPLATING_PREFIX, closingBracket + 8);
    }
    preparedTemplates.put(original, jsonTemplate);
    return jsonTemplate;
  }

  private static int findClosingBracket(String text, int openPos) {
    int closePos = openPos;
    int counter = 1;
    while (counter > 0) {
      char c = text.charAt(++closePos);
      if (c == '(') {
        counter++;
      } else if (c == ')') {
        counter--;
      }
    }
    return closePos;
  }

  public static String generatePayload(String template, Map<String, Object> variables) {
    Context context = new Context();
    context.setVariable("templateUtils", new TemplatingUtils());
    if (variables != null) {
      for (Map.Entry<String, Object> entry : variables.entrySet()) {
        context.setVariable(entry.getKey(), entry.getValue());
      }
    }
    // context.setVariable("firstname", "toto");
    // template = "{\"instance\": 0, \"input\": \"manuel\", \"firstname\":\"[th(${firstname})]\"}";
    return templateEngine.process(prepareThymeleafBlocks(template), context);
  }
}
