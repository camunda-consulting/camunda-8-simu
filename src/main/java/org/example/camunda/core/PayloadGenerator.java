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
  public static final String TEMPLATING_PREFIX = "templateUtils.";
  private static TemplateEngine templateEngine;
  private static Map<String, List<TemplatingMethod>> templatingMethods = null;

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

  public static Map<String, List<TemplatingMethod>> getTemplatingMethods() {
    if (templatingMethods == null) {
      templatingMethods = new HashMap<>();
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
            } else if (arg.getType().equals("long") || arg.getType().equals("java.lang.Long")) {
              example += "1 /*" + arg.getName() + "*/";
            } else if (arg.getType().equals("boolean")
                || arg.getType().equals("java.lang.Boolean")) {
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
    }
    return templatingMethods;
  }

  public static String prepareThymeleafBlocks(String jsonTemplate) {
    Map<String, List<TemplatingMethod>> methods = getTemplatingMethods();
    int tuIndex = jsonTemplate.indexOf(TEMPLATING_PREFIX);
    while (tuIndex > 0) {
      int openingBracket = jsonTemplate.indexOf("(", tuIndex);
      String methodName =
          jsonTemplate.substring(tuIndex + TEMPLATING_PREFIX.length(), openingBracket);
      int closingBracket = findClosingBracket(jsonTemplate, openingBracket);
      String before = jsonTemplate.substring(0, tuIndex);
      String after = jsonTemplate.substring(closingBracket + 1);
      String replace = "[( ${" + jsonTemplate.substring(tuIndex, closingBracket + 1) + "} )]";

      TemplatingMethod tuMethod = methods.get(methodName).get(0);
      if (tuMethod.getResultType().equals("java.lang.String")) {
        replace = "\"" + replace + "\"";
      }
      jsonTemplate = before + replace + after;
      tuIndex = jsonTemplate.indexOf(TEMPLATING_PREFIX, closingBracket + 8);
    }
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
    return getTemplateEngine().process(prepareThymeleafBlocks(template), context);
  }
}
