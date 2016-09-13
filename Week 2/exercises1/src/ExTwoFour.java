import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.*;
import java.util.function.Function;


class ExTwoFour {

	public static void main (String[] args)
	{
		Factorizer f = new Factorizer();
	  	//exerciseFactorizer(new Memoizer1<Long,long[]>(f));
		//exerciseFactorizer(new Memoizer2<Long,long[]>(f));
		//exerciseFactorizer(new Memoizer3<Long,long[]>(f));
		//exerciseFactorizer(new Memoizer4<Long,long[]>(f));
		//exerciseFactorizer(new Memoizer5<Long,long[]>(f));
		exerciseFactorizer(new Memoizer0<Long,long[]>(f));
		//exerciseFactorizer(f);
		System.out.println(f.getCount());
	}

	private static void exerciseFactorizer(Computable<Long, long[]> f)
	{
		final long start = 10_000_000_000L, range = 20_000L;
		final int threadCount = 16;
		Thread[] threads = new Thread[threadCount];

		for (int t=0; t<threadCount; t++)
		{
			long from1 = start;
			long to1 = from1 + range;
			long from2 = start+range+t*range/4;
			long to2 = from2 + range;

			threads[t] = new Thread(() -> {
				try{
					for(long x = from1; x<to1; x++) f.compute(x);
					for(long x = from2; x<to2; x++) f.compute(x);
				} 
				catch(InterruptedException e){System.out.println("fucked");}
			});
		}

		for(Thread thread : threads) thread.start();

		try {
			for (int t=0; t<threadCount; t++) threads[t].join();
		} catch (InterruptedException exn) { }
	}
}

class Memoizer0<A, V> implements Computable<A, V> {
  private final Map<A, V> cache = new ConcurrentHashMap<A, V>();
  private final Computable<A, V> c;
  
  public Memoizer0(Computable<A, V> c) { this.c = c; }
  
  public V compute(A arg) throws InterruptedException {
    V result = cache.computeIfAbsent(arg, (A argv) -> {
      try{
        return c.compute(argv);
      } catch(InterruptedException e) {
        return null;
      }
    });
    
    cache.putIfAbsent(arg, result);
    
    return result;
  }
}



