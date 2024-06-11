package org.example.camunda.core;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.example.camunda.dto.templating.Dataset;
import org.example.camunda.utils.DataUtils;

public class TemplatingUtils {

  private static long tuCounter = 0;
  private long tuId = 0;

  public TemplatingUtils() {
    tuId = tuCounter++;
  }

  private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

  public String getFrom(String datasetName) {
    return getFrom(datasetName, null, null, null);
  }

  public String getFrom(String datasetName, String category) {
    return getFrom(datasetName, category, null, null);
  }

  public String getFrom(String datasetName, String category, String suffix) {
    return getFrom(datasetName, category, suffix, null);
  }

  public String getFrom(String datasetName, String category, String suffix, String prefix) {
    Dataset dataset = DataUtils.getDataset(datasetName);
    Random rand = new Random(tuId);
    if (category == null) {
      String[] categories = dataset.getCategorizedData().keySet().toArray(new String[0]);
      category = categories[rand.nextInt(categories.length)];
    }
    List<String> candidates = dataset.getCategorizedData().get(category);
    String val = candidates.get(rand.nextInt(candidates.size()));
    if (prefix != null) {
      val = prefix + val;
    }
    if (suffix != null) {
      val += suffix;
    }
    return val;
  }

  public String now() {
    return sdf.format(new Date());
  }

  public String generateFriendlyString(long syllabes) {
    String[] consonant = {
      "b", "c", "d", "f", "g", "j", "k", "l", "m", "n", "p", "r", "s", "t", "v", "ph", "ch", "ll"
    };
    String[] vowels = {"a", "e", "i", "o", "u", "ou", "oi", "ui"};
    StringBuffer code = new StringBuffer();
    Random rand = new Random(tuId);

    for (int i = 0; i < syllabes; i++) {
      code.append(consonant[rand.nextInt(17)]);
      code.append(vowels[rand.nextInt(7)]);
    }
    // code.append(rand.nextInt(89) + 10);

    return code.toString();
  }

  private static int counter = 0;

  public String generateUUID(String prefix) {
    // for demo reasons we generate something readable
    if (counter == 0) {
      counter =
          Calendar.getInstance().get(Calendar.MINUTE) + Calendar.getInstance().get(Calendar.SECOND);
    } else {
      counter++;
    }
    String result = prefix + Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + counter;
    return result;
  }

  public long range(long min, long max) {
    long dif = max - min;
    return min + Math.round(Math.random() * dif);
  }

  private static long sequence = 0L;

  public long sequentialNumber() {
    return sequence++;
  }

  private static Map<String, NormalDistribution> normalDistributionRegistry = new HashMap<>();

  private NormalDistribution getNormalDistribution(double mean, double standardDeviation) {
    String name = mean + "_" + standardDeviation;
    NormalDistribution normalDistribution = normalDistributionRegistry.get(name);
    if (normalDistribution == null) {
      normalDistribution = new NormalDistribution(mean, standardDeviation);
      normalDistributionRegistry.put(name, normalDistribution);
    }
    return normalDistribution;
  }

  public Double normal(double mean, double standardDeviation) {
    return getNormalDistribution(mean, standardDeviation).sample();
  }
}
