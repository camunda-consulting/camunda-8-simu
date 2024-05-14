package org.example.camunda.utils;

import java.util.HashMap;
import java.util.Map;
import org.camunda.feel.FeelEngine;
import org.camunda.feel.impl.SpiServiceLoader;
import org.camunda.feel.syntaxtree.Val;
import scala.util.Either;

public class FeelUtils {

  private static FeelEngine engine =
      new FeelEngine.Builder()
          .valueMapper(SpiServiceLoader.loadValueMapper())
          .functionProvider(SpiServiceLoader.loadFunctionProvider())
          .build();

  public static Object evaluate(String expression) {
    return evaluate(expression, new HashMap<>());
  }

  public static Object evaluate(String expression, Map<String, Object> context) {
    final Either<FeelEngine.Failure, Object> result = engine.evalExpression(expression, context);
    if (result.isLeft()) {
      return result.left().get().message();
    }
    return result.getOrElse(null);
  }

  public static <T extends Val> T evaluate(String expression, Class<T> expectedType) {
    return evaluate(expression, new HashMap<>(), expectedType);
  }

  public static <T extends Val> T evaluate(
      String expression, Map<String, Object> context, Class<T> expectedType) {
    return (T) evaluate(expression, context);
  }
}
