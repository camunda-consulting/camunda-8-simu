package org.example.camunda.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.example.camunda.exception.TechnicalException;

public class JsonUtils {

  private JsonUtils() {}

  private static ObjectMapper mapper;

  public static Object eventuallyJsonNode(String someString) {
    return toJsonNode(someString);
  }

  public static JsonNode toJsonNode(InputStream is) throws IOException {
    return getObjectMapper().readTree(is);
  }

  public static ObjectNode toJsonNode(String json) {
    try {
      return (ObjectNode) getObjectMapper().readTree(json);
    } catch (JsonProcessingException e) {
      ObjectNode node = getObjectMapper().createObjectNode();
      node.put("error", "Exception reading the template : " + e.getLocalizedMessage());
      return node;
    }
  }

  public static <T> T toObject(String json, Class<T> type) {
    try {
      return getObjectMapper().readValue(json, type);
    } catch (JsonProcessingException e) {
      throw new TechnicalException(e);
    }
  }

  public static <T> T toParametrizedObject(String json, TypeReference<T> type) {
    try {
      return getObjectMapper().readValue(json, type);
    } catch (JsonProcessingException e) {
      throw new TechnicalException(e);
    }
  }

  public static String toJson(Object object) throws IOException {
    return getObjectMapper().writeValueAsString(object);
  }

  public static void toJsonFile(Path path, Object object) throws IOException {
    if (!Files.exists(path.getParent())) {
      Files.createDirectories(path.getParent());
    }
    getObjectMapper().writerWithDefaultPrettyPrinter().writeValue(path.toFile(), object);
  }

  public static <T> T fromJsonFile(Path path, Class<T> type)
      throws StreamReadException, DatabindException, IOException {
    return getObjectMapper().readValue(path.toFile(), type);
  }

  public static <T> T fromJsonFile(Path path, TypeReference<T> type)
      throws StreamReadException, DatabindException, IOException {
    return getObjectMapper().readValue(path.toFile(), type);
  }

  private static ObjectMapper getObjectMapper() {
    if (mapper == null) {
      mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    return mapper;
  }
}
