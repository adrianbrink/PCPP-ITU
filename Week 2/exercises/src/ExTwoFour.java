import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


class ExTwoFour {

    public static void main(String[] args) {
        Factorizer f = new Factorizer();
        //exerciseFactorizer(new Memoizer1<Long,long[]>(f));
        //exerciseFactorizer(new Memoizer2<Long,long[]>(f));
        //exerciseFactorizer(new Memoizer3<Long,long[]>(f));
        //exerciseFactorizer(new Memoizer4<Long,long[]>(f));
        //exerciseFactorizer(new Memoizer5<Long,long[]>(f));
        exerciseFactorizer(new Memoizer0<Long, long[]>(f));
        //exerciseFactorizer(f);
        System.out.println(f.getCount());
    }

    private static void exerciseFactorizer(Computable<Long, long[]> f) {
        final long start = 10_000_000_000L, range = 20_000L;
        final int threadCount = 16;
        Thread[] threads = new Thread[threadCount];

        for (int t = 0; t < threadCount; t++) {
            long from1 = start;
            long to1 = from1 + range;
            long from2 = start + range + t * range / 4;
            long to2 = from2 + range;

            threads[t] = new Thread(() -> {
                try {
                    for (long x = from1; x < to1; x++) f.compute(x);
                    for (long x = from2; x < to2; x++) f.compute(x);
                } catch (InterruptedException e) {
                    System.out.println("fucked");
                }
            });
        }

        for (Thread thread : threads) thread.start();

        try {
            for (int t = 0; t < threadCount; t++) threads[t].join();
        } catch (InterruptedException exn) {
        }
    }
}



