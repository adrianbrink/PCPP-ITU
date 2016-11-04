import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Iterator;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.IntStream;
import java.util.concurrent.*;
import java.util.ArrayList;

public class Ex81 {

	static int threadCount = 16, range = 99, perThread = 1000, stripeCount = 7, bucketCount = 77;
	static StripedWriteMap<Integer, String> map = new StripedWriteMap<Integer,String>(bucketCount, stripeCount);
	//static WrapConcurrentHashMap<Integer, String> map = new WrapConcurrentHashMap<Integer,String>();
	static CyclicBarrier barrier = new CyclicBarrier(threadCount);
	static int[] counts = new int[threadCount];
	static final ArrayList<java.util.function.Consumer<WrappingConsumer>> actions = new ArrayList<java.util.function.Consumer<WrappingConsumer>>();

	public static void main(String[] args) {
		parallelMapTest();
	}

	private static void parallelMapTest()
	{
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

       	defineActions();

		final AtomicInteger sum = new AtomicInteger(0); // sum of all keys put into map minus removed

		for (int i = 0; i< threadCount; i++) {
            final int threadNumber = i;
            executor.execute(() -> sum.addAndGet( runThread(threadNumber) ) );
        };

        try {
	        executor.shutdown();
	        executor.awaitTermination(100, TimeUnit.DAYS);
    	} catch (InterruptedException e) {}
		
		confirmResult(sum);
	}

	private static void defineActions()
	{
		actions.add(t -> {
            String result = map.putIfAbsent(t.key, t.value);
            if (result == null) {
                t.sum.addAndGet(t.key);
                t.counts[t.id]++;
            }
        });

       actions.add(t -> {
            String result = map.put(t.key, t.value);
            if (result == null) {
                t.sum.addAndGet(t.key);
            } else {
                t.counts[Integer.parseInt(result.split(":")[0])]--;
            }
            t.counts[t.id]++;
       });

       actions.add(t -> {
            String result = map.remove((Integer) t.key);
            if (result != null) {
                t.sum.addAndGet(-t.key);
                t.counts[Integer.parseInt(result.split(":")[0])]--;
            }
        });

       actions.add(t -> map.containsKey(t.key));
	}

	private static int runThread(int threadNr){
		
		try { barrier.await(); } // wait for all threads to be ready
		catch (Exception exn) { }

		Random random = new Random();	
		AtomicInteger threadSum = new AtomicInteger(0);

		for (int x = 0; x < perThread; x++) {
            int action = random.nextInt( actions.size() );
            int key = random.nextInt(range);
           actions.get(action).accept(new WrappingConsumer(
                    key,
                    threadSum,
                    threadNr + ":" + key,
                    counts,
                    threadNr
           ));
        }

        return threadSum.get();
	}

	private static void confirmResult(AtomicInteger sum)
	{
		final AtomicInteger mapSum = new AtomicInteger(0);
		map.forEach( (k, v) -> mapSum.getAndAdd(k) );
      
		System.out.println("Thread says: " + sum);
        System.out.println("actual sum: " + mapSum);
        System.out.println("counts array sum " + IntStream.of(counts).sum() );
        
        //map.forEach( (k, v) -> System.out.printf("%10d maps to thread %s%n", k, v) );
        
        // check that each thread has correct nr of entries in map
        int[] mapCounts = new int[threadCount];
        map.forEach( (k, v) -> mapCounts[Integer.parseInt(v.split(":")[0])] ++ ); 

        for(int i = 0; i<threadCount; i++)
        {
        	System.out.println(mapCounts[i] + " -- " + counts[i]);
        }
	}
}

class WrappingConsumer {
    public final int key;
    public final AtomicInteger sum;
    public final String value;
    public final int[] counts;
    public final int id;

    WrappingConsumer(int key, AtomicInteger sum, String value, int[] counts, final int id) {
        this.key = key;
        this.sum = sum;
        this.value = value;
        this.counts = counts;
        this.id = id; // thread id
    }
}