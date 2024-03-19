package org.example.camunda.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

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
}
