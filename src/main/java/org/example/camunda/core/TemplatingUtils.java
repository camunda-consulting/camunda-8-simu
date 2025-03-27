package org.example.camunda.core;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.example.camunda.dto.templating.Dataset;
import org.example.camunda.dto.templating.JsonDataset;
import org.example.camunda.utils.ContextUtils;
import org.example.camunda.service.DatasetUtils;
import org.example.camunda.utils.JsonUtils;

public class TemplatingUtils {

  private static long tuCounter = 0;
  private long tuId = 0;

  public TemplatingUtils() {
    tuId = tuCounter++;
  }

  private static final SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

  public String getJsonDataset(String datasetName) throws IOException {
    JsonDataset dataset = DatasetUtils.getJsonDataset(datasetName);
    return "[" + String.join(", ", dataset.getData()) + "]";
  }

  public String getFromJsonDataset(String datasetName, String key) throws IOException {
    JsonDataset dataset = DatasetUtils.getJsonDataset(datasetName);
    Random rand = new Random(tuId);
    int index = rand.nextInt(dataset.getData().size());
    Map<String, Object> data =
        JsonUtils.toParametrizedObject(
            dataset.getData().get(index), new TypeReference<Map<String, Object>>() {});
    return getAttribute(data, key);
  }

  private static String getAttribute(Map<String, Object> object, String key) {
    int dotIndex = key.indexOf(".");
    if (dotIndex > 0) {
      Map<String, Object> subMap = (Map) object.get(key.substring(0, dotIndex));
      return getAttribute(subMap, key.substring(dotIndex + 1));
    }
    return (String) object.get(key);
  }

  public String getFrom(String datasetName) throws IOException {
    return getFrom(datasetName, null, null, null);
  }

  public String getFrom(String datasetName, String category) throws IOException {
    return getFrom(datasetName, category, null, null);
  }

  public String getFrom(String datasetName, String category, String suffix) throws IOException {
    return getFrom(datasetName, category, suffix, null);
  }

  public String getFrom(String datasetName, String category, String suffix, String prefix) throws IOException {
    Dataset dataset = DatasetUtils.getDataset(datasetName);
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

  private static final AtomicInteger counter =
      new AtomicInteger(
          Calendar.getInstance().get(Calendar.MINUTE)
              + Calendar.getInstance().get(Calendar.SECOND));

  public String generateUUID(String prefix) {
    // for demo reasons we generate something readable
    return prefix + Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + counter.addAndGet(1);
  }

  public long range(long min, long max) {
    long dif = max - min;
    return min + Math.round(Math.random() * dif);
  }

  private static final AtomicLong sequence = new AtomicLong(0L);

  public long sequentialNumber() {
    return sequence.addAndGet(1);
  }

  private static final Map<String, NormalDistribution> normalDistributionRegistry = new HashMap<>();

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

  public String randomFromArray(String... objects) {
    if (objects == null || objects.length == 0) return null;
    return objects[(int) (Math.random() * objects.length)];
  }

  public String betaFromArray(int alpha, int beta, String... objects) {
    BetaDistribution bd = new BetaDistribution(alpha, beta);
    if (objects == null || objects.length == 0) return null;
    return objects[(int) (bd.sample() * objects.length)];
  }

  public String normalFromArray(double mean, double standardDeviation, String... objects) {
    NormalDistribution nd = null;

    if (mean == 0 && standardDeviation == 0) {
      nd = new NormalDistribution(0.5, 0.30);
    } else {
      nd = new NormalDistribution(mean, standardDeviation);
    }
    if (objects == null || objects.length == 0) return null;

    int index = (int) (nd.sample() * objects.length);

    if (index >= objects.length) {
      return objects[objects.length - 1];
    } else {
      if (index < 0) {
        return objects[0];
      } else {
        return objects[index];
      }
    }
  }

  public String uniformBirthdate(int minAge, int maxAge) {
    Calendar calMin = Calendar.getInstance();
    calMin.setTimeInMillis(ContextUtils.getEngineTime());
    calMin.set(Calendar.HOUR_OF_DAY, 0);
    calMin.set(Calendar.MINUTE, 0);
    calMin.set(Calendar.SECOND, 0);
    calMin.set(Calendar.MILLISECOND, 0);
    Calendar calMax = (Calendar) calMin.clone();
    calMin.add(Calendar.YEAR, -maxAge);
    calMax.add(Calendar.YEAR, -minAge);
    long minMillis = calMin.getTimeInMillis();
    long maxMillis = calMax.getTimeInMillis();
    long chosenMillis = minMillis + (long) (Math.random() * (maxMillis - minMillis));
    return sdf.format(Date.from(Instant.ofEpochMilli(chosenMillis)));
  }

  public boolean uniformBoolean() {
    return Math.random() < 0.5;
  }

  public boolean randBool(double probability) {
    return uniformBooleanByProbability(probability);
  }

  public boolean uniformBooleanByProbability(double probability) {
    return Math.random() < probability;
  }

  public String nowPlusMillis(int millis) {
    return nowPlusPeriod(Duration.ofMillis(millis));
  }

  public String nowPlusSeconds(int seconds) {
    return nowPlusPeriod(Duration.ofSeconds(seconds));
  }

  public String nowPlusMinutes(int minutes) {
    return nowPlusPeriod(Duration.ofMinutes(minutes));
  }

  public String nowPlusHours(int hours) {
    return nowPlusPeriod(Duration.ofHours(hours));
  }

  public String nowPlusDays(int days) {
    return nowPlusPeriod(Period.ofDays(days));
  }

  public String nowPlusWeeks(int weeks) {
    return nowPlusPeriod(Period.ofWeeks(weeks));
  }

  public String nowPlusMonths(int months) {
    return nowPlusPeriod(Period.ofMonths(months));
  }

  public String nowPlusYears(int years) {
    return nowPlusPeriod(Period.ofYears(years));
  }

  public String nowPlusPeriod(TemporalAmount amount) {
    long time =
        ContextUtils.getEngineTime() > 0
            ? ContextUtils.getEngineTime()
            : System.currentTimeMillis();
    return sdf.format(
        Date.from(
            Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).plus(amount).toInstant()));
  }

  public String now() {
    if (ContextUtils.getEngineTime() > 0) {
      return sdf.format(new Date(ContextUtils.getEngineTime()));
    }
    return sdf.format(new Date());
  }

  public String todayPlusDays(int days) {
    return todayPlusPeriod(Period.ofDays(days));
  }

  public String todayPlusWeeks(int weeks) {
    return todayPlusPeriod(Period.ofWeeks(weeks));
  }

  public String todayPlusMonths(int months) {
    return todayPlusPeriod(Period.ofMonths(months));
  }

  public String todayPlusYears(int years) {
    return todayPlusPeriod(Period.ofYears(years));
  }

  public String todayPlusPeriod(TemporalAmount amount) {
    long time =
        ContextUtils.getEngineTime() > 0
            ? ContextUtils.getEngineTime()
            : System.currentTimeMillis();
    return dateSdf.format(
        Date.from(
            Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).plus(amount).toInstant()));
  }

  public String today() {
    if (ContextUtils.getEngineTime() > 0) {
      return dateSdf.format(new Date(ContextUtils.getEngineTime()));
    }
    return dateSdf.format(new Date());
  }
}
