// For week 2
// sestoft@itu.dk * 2014-09-04

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

class SimpleHistogram {
    final static Histogram histogram = new Histogram2(1000);

  public static void main(String[] args) {
      countParallelN(5_000, 4);
    dump(histogram);
  }

  public static void dump(Histogram histogram) {
    int totalCount = 0;
    for (int bin=0; bin<histogram.getSpan(); bin++) {
      System.out.printf("%4d: %9d%n", bin, histogram.getCount(bin));
      totalCount += histogram.getCount(bin);
    }
    System.out.printf("      %9d%n", totalCount);
  }

    // General parallel solution, using multiple threads
    private static long countParallelN(int range, int threadCount) {
      final int perThread = range / threadCount;
      final LongCounter lc = new LongCounter();
      Thread[] threads = new Thread[threadCount];
      for (int t=0; t<threadCount; t++) {
        final int from = perThread * t,
          to = (t+1==threadCount) ? range : perThread * (t+1);
        threads[t] = new Thread(() -> {
          for (int i=from; i<to; i++) {
              int count = (int) countSequential(i);
              histogram.increment(count);
          }
        });
      }
      for (int t=0; t<threadCount; t++)
        threads[t].start();
      try {
        for (int t=0; t<threadCount; t++)
          threads[t].join();
      } catch (InterruptedException exn) { }
      return lc.get();
    }

    // Sequential solution
    private static long countSequential(int range) {
      long count = 0;
      final int from = 0, to = range;
      for (int i=from; i<to; i++)
        if (isPrime(i))
          count++;
      return count;
    }

    private static boolean isPrime(int n) {
      int k = 2;
      while (k * k <= n && n % k != 0)
        k++;
      return n >= 2 && k * k > n;
    }
}

interface Histogram {
  public void increment(int bin);
  public int getCount(int bin);
  public int getSpan();
}

class Histogram1 implements Histogram {
  private int[] counts;
  public Histogram1(int span) {
    this.counts = new int[span];
  }
  public void increment(int bin) {
    counts[bin] = counts[bin] + 1;
  }
  public void incrementBy(int bin, int amount) {
    counts[bin] = counts[bin] + amount;
  }
  public int getCount(int bin) {
    return counts[bin];
  }
  public int getSpan() {
    return counts.length;
  }
}

class Histogram2 implements Histogram {
  private final int[] counts; // needs to be final since the number of bins should never change
  public Histogram2(int span) {
    this.counts = new int[span];
  }
  public synchronized void increment(int bin) {
    counts[bin] = counts[bin] + 1;
  }
  public synchronized void incrementBy(int bin, int amount) {
    counts[bin] = counts[bin] + amount;
  }
  public synchronized int getCount(int bin) {
    return counts[bin];
  }
  public int getSpan() {
    return counts.length;
  }
}

class LongCounter {
  private long count = 0;
  public synchronized void increment() {
    count = count + 1;
  }
  public synchronized long get() {
    return count;
  }
}
