import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import static org.multiverse.api.StmUtils.atomic;
import static org.multiverse.api.StmUtils.newTxnInteger;
import org.multiverse.api.references.TxnInteger;


/**
 * Created by jtuxen on 13/11/15.
 */
public class TestCasHistogram {
    public static void main(String[] args) {
        new TestStmHistogram().main(args);
    }
}

class TestStmHistogram {
    public static void main(String[] args) {
        System.out.println("Starting TestStmHistogram");
        Histogram one = new StmHistogram(30);
        countPrimeFactorsWithStmHistogram(one);

        Histogram two = new CasHistogram(30);
        countPrimeFactorsWithStmHistogram(two);

        one = new StmHistogram(30);
        countPrimeFactorsWithStmHistogram(one);

        two = new CasHistogram(30);
        countPrimeFactorsWithStmHistogram(two);
    }

    private static void countPrimeFactorsWithStmHistogram(Histogram histogram) {
        final int range = 4_000_000;
        final int threadCount = 10, perThread = range / threadCount;
        final CyclicBarrier startBarrier = new CyclicBarrier(threadCount + 1),
                stopBarrier = startBarrier;
        final Thread[] threads = new Thread[threadCount];
        for (int t = 0; t < threadCount; t++) {
            final int from = perThread * t,
                    to = (t + 1 == threadCount) ? range : perThread * (t + 1);
            threads[t] =
                    new Thread(() -> {
                        try {
                            startBarrier.await();
                        } catch (Exception exn) {
                        }
                        for (int p = from; p < to; p++)
                            histogram.increment(countFactors(p));

                        System.out.print("*");
                        try {
                            stopBarrier.await();
                        } catch (Exception exn) {
                        }
                    });
            threads[t].start();
        }
        try {
            startBarrier.await();
        } catch (Exception exn) {
        }
        Timer t = new Timer();


        try {
            stopBarrier.await();
        } catch (Exception exn) {
        }
        double end = t.check();
        System.out.println(histogram.getClass()+" :"+end);

        //dump(histogram);
    }

    public static void dump(Histogram histogram) {
        int totalCount = 0;
        for (int bin = 0; bin < histogram.getSpan(); bin++) {
            System.out.printf("%4d: %9d%n", bin, histogram.getCount(bin));
            totalCount += histogram.getCount(bin);
        }
        System.out.printf("      %9d%n", totalCount);
    }

    public static int countFactors(int p) {
        if (p < 2)
            return 0;
        int factorCount = 1, k = 2;
        while (p >= k * k) {
            if (p % k == 0) {
                factorCount++;
                p /= k;
            } else
                k++;
        }
        return factorCount;
    }
}

class StmHistogram implements Histogram {
    private final TxnInteger[] counts;
    private final Integer span;

    public StmHistogram(int span) {
        counts = new TxnInteger[200];
        for (int i = 0; i < 200; i++) counts[i] = newTxnInteger(0);
        this.span = span;
    }

    public void increment(int bin) {
        atomic(() -> counts[bin].increment());
    }

    public int getCount(int bin) {
        return atomic(() -> counts[bin].get());
    }

    public int getSpan() {
        return span;
    }

    public int[] getBins() {
        final int[] arr = new int[span];

        for (int i = 0; i<span; i++) {
            arr[i] = getCount(i); //internally this uses atomic();
        }
        return arr;
    }

    public int getAndClear(int bin) {
        return atomic(() -> {
            final int value = counts[bin].getAndSet(0);
            return value;
        });
    }

    @Override
    public void transferBin(Histogram hist) {
        for (int i=0; i<span; i++) {
            final int bin = i;
            atomic(() -> counts[bin].increment(hist.getAndClear(bin)));
        }
    }
}