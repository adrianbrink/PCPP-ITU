// For week 2
// sestoft@itu.dk * 2014-09-04

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

class SimpleHistogram {
    public static void main(String[] args) {
        final int range = 100_000;
        final int threadCount = 4;
//        final Histogram histogram = new Histogram2(30);
        final Histogram histogram = new Histogram3(30);


        Thread[] threads = new Thread[threadCount];
        ArrayList<Integer> inputList = new ArrayList<>(range);
        for (int i = 0; i < range; i++) {
            inputList.add(i, i);
        }
        Collections.shuffle(inputList);

        for (int i = 0; i < threadCount; i++) {
            int perThread = range / threadCount;
            int from = perThread * i;
            int to = (i + 1 == threadCount) ? range : perThread * (i + 1);
            List<Integer> list = inputList.subList(from, to);
            threads[i] = new Thread(() -> {
                for (Integer number : list) {
                    int result = PrimeCounter.primeFactors(number).size();
                    histogram.increment(result);
                }
            });
        }

        for (int t = 0; t < threadCount; t++) {
            threads[t].start();
        }

        try {
            for (int t = 0; t < threadCount; t++) {
                threads[t].join();
            }
        } catch (InterruptedException exn) {
            System.out.println("something fucked up");
        }

        System.out.println(PrimeCounter.countSequential(range));
        dump(histogram);
    }

    public static void countPrimeFactor(int range, Histogram histo) {
        for (int i = 0; i < range; i++) {
            Set<Integer> count = PrimeCounter.primeFactors((long) i);
            histo.increment(count.size());
        }
    }

    public static void dump(Histogram histogram) {
        int totalCount = 0;
        for (int bin = 0; bin < histogram.getSpan(); bin++) {
            System.out.printf("%4d: %9d%n", bin, histogram.getCount(bin));
            totalCount += histogram.getCount(bin);
        }
        System.out.printf("      %9d%n", totalCount);
    }
}

class PrimeCounter {

    static Set<Integer> primeFactors(long number) {
        Set<Integer> primefactors = new HashSet<>();
        long copyOfInput = number;
        for (int i = 2; i <= copyOfInput; i++) {
            if (copyOfInput % i == 0) {
                primefactors.add(i); // prime factor
                copyOfInput /= i;
                i--;
            }
        }
        return primefactors;
    }
    // Sequential solution
    static long countSequential(int range) {
        long count = 0;
        final int from = 0, to = range;
        for (int i = from; i <= to; i++)
            if (isPrime(i))
                count++;
        return count;
    }

    static boolean isPrime(int n) {
        int k = 2;
        while (k * k <= n && n % k != 0)
            k++;
        return n >= 2 && k * k > n;
    }
}

interface Histogram {
    void increment(int bin);
    int getCount(int bin);
    int getSpan();
}

class Histogram1 implements Histogram {
    private int[] counts;

    public Histogram1(int span) {
        this.counts = new int[span];
    }

    public void increment(int bin) {
        counts[bin] = counts[bin] + 1;
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

    public synchronized int getCount(int bin) {
        return counts[bin];
    }

    public int getSpan() {
        return counts.length;
    }
}

class Histogram3 implements Histogram {
    private final AtomicInteger[] counts; // needs to be final since the number of bins should never change

    public Histogram3(int span) {
        this.counts = new AtomicInteger[span];
        for (int i = 0; i < span; i++) {
            counts[i] = new AtomicInteger(0);
        }
    }

    public void increment(int bin) {
        counts[bin].addAndGet(1);
    }

    public  int getCount(int bin) {
        return counts[bin].get();
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
