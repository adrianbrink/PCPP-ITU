// For week 2
// sestoft@itu.dk * 2014-09-04

import java.util.HashSet;
import java.util.Set;

class SimpleHistogram {
    public static void main(String[] args) {
        final int range = 100_000;
        final Histogram histogram = new Histogram2(30);

        System.out.println(PrimeCounter.countSequential(range));
        countPrimeFactor(range, histogram);
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
                // Found a prime factor, add it to the bin
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
        for (int i = from; i < to; i++)
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

class LongCounter {
    private long count = 0;

    public synchronized void increment() {
        count = count + 1;
    }

    public synchronized long get() {
        return count;
    }
}
