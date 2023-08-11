package org.R2Useful;

public class DescriptiveStatisticsUtility {

  private boolean useFloating = false;

  private long nNumbers;

  private long longTotal;
  private long longSquaredTotal;
  private long longMin;
  private long longMax;

  private double doubleTotal;
  private double doubleSquaredTotal;
  private double doubleMin;
  private double doubleMax;

  public DescriptiveStatisticsUtility(boolean useFloating) {
    this.useFloating = useFloating;

    nNumbers = 0;

    longTotal = 0;
    longSquaredTotal = 0;
    longMin = Long.MAX_VALUE;
    longMax = Long.MIN_VALUE;

    doubleTotal = 0;
    doubleSquaredTotal = 0;
    doubleMin = Double.POSITIVE_INFINITY;
    doubleMax = Double.NEGATIVE_INFINITY;
  }

  public void reset() {
    nNumbers = 0;

    longTotal = 0;
    longSquaredTotal = 0;
    longMin = Long.MAX_VALUE;
    longMax = Long.MIN_VALUE;

    doubleTotal = 0;
    doubleSquaredTotal = 0;
    doubleMin = Double.POSITIVE_INFINITY;
    doubleMax = Double.NEGATIVE_INFINITY;
  }

  public void useDoubleValue(double value) throws Exception {
    if (useFloating) {
      doubleTotal += value;
      doubleSquaredTotal += value * value;
      if (value > doubleMax) {
        doubleMax = value;
      }
      if (value < doubleMin) {
        doubleMin = value;
      }

      nNumbers++;
    } else {
      System.out.println(this + " cannot store a floating number when instantiated for integers.");
      throw new Exception();
    }
  }

  public void useLongValue(long value) throws Exception {
    if (!useFloating) {
      longTotal += value;
      longSquaredTotal += value * value;
      if (value > longMax) {
        longMax = value;
      }
      if (value < longMin) {
        longMin = value;
      }

      nNumbers++;
    } else {
      System.out.println(this + " cannot store an integer number when instantiated for floating.");
      throw new Exception();
    }
  }

  public double getMinAsDouble() {
    if (useFloating) {
      return doubleMin;
    }
    return longMin;
  }

  public long getMinAsLong() {
    if (useFloating) {
      return (long) doubleMin;
    }
    return longMin;
  }

  public double getMaxAsDouble() {
    if (useFloating) {
      return doubleMax;
    }
    return longMax;
  }

  public long getMaxAsLong() {
    if (useFloating) {
      return (long) doubleMax;
    }
    return longMax;
  }

  public double getMean() {
    if (useFloating) {
      return doubleTotal / nNumbers;
    }
    return ((double) longTotal) / nNumbers;
  }

  public double getStd() {
    if (useFloating) {
      double mean = getMean();

      return Math.sqrt(doubleSquaredTotal / nNumbers - mean * mean);
    }
    double mean = getMean();
    return Math.sqrt(((double) longSquaredTotal) / nNumbers - mean * mean);
  }

  public long getN() {
    return nNumbers;
  }

  public long getTotalLong() {
    if (useFloating) {
      return (long) doubleTotal;
    }
    return this.longTotal;
  }

  public double getTotalDouble() {
    if (useFloating) {
      return this.doubleTotal;
    }
    return this.longTotal;
  }

  public String getAllPretty() {
    if (useFloating) {
      return ("min = "
          + this.getMinAsDouble()
          + " max = "
          + this.getMaxAsDouble()
          + " mean = "
          + getMean()
          + " std = "
          + getStd()
          + " n = "
          + nNumbers);
    }
    return ("min = "
        + this.getMinAsLong()
        + " max = "
        + this.getMaxAsLong()
        + " mean = "
        + getMean()
        + " std = "
        + getStd()
        + " n = "
        + nNumbers);
  }
}
