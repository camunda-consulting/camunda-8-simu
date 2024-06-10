package org.example.camunda.dto.templating;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class Dataset {
  String name;
  Map<String, List<String>> categorizedData;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, List<String>> getCategorizedData() {
    return categorizedData;
  }

  public void setCategorizedData(Map<String, List<String>> categorizedData) {
    this.categorizedData = categorizedData;
  }

  public String getRandom(String category, String suffix, String prefix) {
    Random rand = new Random(Thread.currentThread().getId() + System.currentTimeMillis() / 10);
    if (category == null) {
      String[] categories = categorizedData.keySet().toArray(new String[0]);
      category = categories[rand.nextInt(categories.length)];
    }
    List<String> candidates = categorizedData.get(category);
    String val = candidates.get(rand.nextInt(candidates.size()));
    if (prefix != null) {
      val = prefix + val;
    }
    if (suffix != null) {
      val += suffix;
    }
    return val;
  }
}
