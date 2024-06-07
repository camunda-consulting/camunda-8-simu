package org.example.camunda.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.commons.math3.distribution.NormalDistribution;

public class TemplatingUtils {

  private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

  public static String firstname() {
    return generateFriendlyString(3 + Math.round(Math.random() * 2));
  }

  public static String lastname() {
    return generateFriendlyString(1 + Math.round(Math.random() * 2));
  }

  public static String now() {
    return sdf.format(new Date());
  }

  public static String generateFriendlyString(long syllabes) {
    String[] consonant = {
      "b", "c", "d", "f", "g", "j", "k", "l", "m", "n", "p", "r", "s", "t", "v", "ph", "ch", "ll"
    };
    String[] vowels = {"a", "e", "i", "o", "u", "ou", "oi", "ui"};
    StringBuffer code = new StringBuffer();
    Random rand = new Random(System.currentTimeMillis());

    for (int i = 0; i < syllabes; i++) {
      code.append(consonant[rand.nextInt(17)]);
      code.append(vowels[rand.nextInt(7)]);
    }
    // code.append(rand.nextInt(89) + 10);

    return code.toString();
  }

  private static int counter = 0;

  public static String generateUUID(String prefix) {
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

  public static long range(long min, long max) {
    long dif = max - min;
    return min + Math.round(Math.random() * dif);
  }

  private static long sequence = 0L;

  public static long sequentialNumber() {
    return sequence++;
  }

  private static Map<String, NormalDistribution> normalDistributionRegistry = new HashMap<>();

  private static NormalDistribution getNormalDistribution(double mean, double standardDeviation) {
    String name = mean + "_" + standardDeviation;
    NormalDistribution normalDistribution = normalDistributionRegistry.get(name);
    if (normalDistribution == null) {
      normalDistribution = new NormalDistribution(mean, standardDeviation);
      normalDistributionRegistry.put(name, normalDistribution);
    }
    return normalDistribution;
  }

  public static Double normal(double mean, double standardDeviation) {
    return getNormalDistribution(mean, standardDeviation).sample();
  }
}
