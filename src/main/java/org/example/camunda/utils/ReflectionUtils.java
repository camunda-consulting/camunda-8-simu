package org.example.camunda.utils;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.client.impl.http.HttpClient;
import io.camunda.zeebe.process.test.engine.InMemoryEngine;
import io.camunda.zeebe.scheduler.clock.ControlledActorClock;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtils {

  public static HttpClient getHttpClient(ZeebeClient client)
      throws NoSuchFieldException,
          SecurityException,
          IllegalArgumentException,
          IllegalAccessException {
    Field f = client.getClass().getDeclaredField("httpClient"); // NoSuchFieldException
    f.setAccessible(true);
    return (HttpClient) f.get(client);
  }

  public static JsonMapper getJsonMapper(ZeebeClient client)
      throws NoSuchFieldException,
          SecurityException,
          IllegalArgumentException,
          IllegalAccessException {
    Field f = client.getClass().getDeclaredField("jsonMapper");
    f.setAccessible(true);
    return (JsonMapper) f.get(client);
  }

  public static ControlledActorClock getClock(InMemoryEngine engine)
      throws NoSuchFieldException,
          SecurityException,
          IllegalArgumentException,
          IllegalAccessException {
    Field f = engine.getClass().getDeclaredField("clock");
    f.setAccessible(true);
    return (ControlledActorClock) f.get(engine);
  }

  public static Object genericInvokeMethod(Object obj, String methodName, Object... params) {
    int paramCount = params.length;
    Method method = null;
    Object requiredObj = null;
    Class<?>[] classArray = new Class<?>[paramCount];
    for (int i = 0; i < paramCount; i++) {
      classArray[i] = params[i].getClass();
    }
    try {
      method = obj.getClass().getDeclaredMethod(methodName, classArray);

    } catch (NoSuchMethodException e) {
      for (Method m : obj.getClass().getDeclaredMethods()) {
        if (m.getName().equals(methodName)) {
          method = m;
          break;
        }
      }
    }
    try {
      method.setAccessible(true);
      requiredObj = method.invoke(obj, params);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }

    return requiredObj;
  }
}
