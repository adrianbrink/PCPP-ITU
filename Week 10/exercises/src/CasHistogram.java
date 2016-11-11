import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by adrianbrink on 11/11/2016.
 */
public class CasHistogram implements Histogram {
    private AtomicInteger[] bins;
    private int span;

    public CasHistogram(int span) {
        this.span = span;
        bins = new AtomicInteger[span];
        for (int i = 0; i < span; i++) {
            new Object(); new Object(); new Object(); new Object();
            bins[i] = new AtomicInteger(0);
        }
    }

    @Override
    public void increment(int bin) {
        int oldValue;
        do {
            oldValue = bins[bin].get();
        } while (!bins[bin].compareAndSet(oldValue, oldValue+1));
    }

    @Override
    public int getCount(int bin) {
        return bins[bin].get();
    }

    @Override
    public int getSpan() {
        return span;
    }

    @Override
    public int[] getBins() {
        final int[] newValue = new int[span];
        for (int i = 0; i < span; i++) {
            newValue[i] = bins[i].get();
        }
        return newValue;
    }

    @Override
    public int getAndClear(int bin) {
        int oldValue;
        do {
            oldValue = bins[bin].get();
        } while (!bins[bin].compareAndSet(oldValue, 0));
        return oldValue;
    }

    @Override
    public void transferBin(Histogram hist) {
        for (int i = 0; i < hist.getSpan(); i++) {
            int delta = hist.getAndClear(0);
            int oldValue, newValue;
            do {
                oldValue = this.getCount(i);
                newValue = oldValue + delta;
            } while (!bins[i].compareAndSet(oldValue, newValue));
        }
    }

    public static void main(String[] args) {
        System.out.print("ss");
    }
}

interface Histogram {
    void increment(int bin);
    int getCount(int bin);
    int getSpan();
    int[] getBins();
    int getAndClear(int bin);
    void transferBin(Histogram hist);
}