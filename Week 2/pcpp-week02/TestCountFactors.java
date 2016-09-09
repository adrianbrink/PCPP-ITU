// For week 2
// sestoft@itu.dk * 2014-08-29

class TestCountFactors {
  public static void main(String[] args) {

    MyAtomicInteger idx = new MyAtomicInteger();
 
    MyAtomicInteger count = new MyAtomicInteger();
    Thread[] threads = new Thread[10];

    for (int i = 0; i <= 9; i++) {
      System.out.println(idx.get());
      int val = 500_000*idx.get();
      int val1 = 500_000*(idx.get()+1);
      threads[i] = new Thread(() -> {
        System.out.println("Start: " + val + " " + val1);
        for(int x = val; x<val1; x++) {
          count.addAndGet(countFactors(x));
        }
      });
      idx.addAndGet(1);
      System.out.println(idx.get());
    }

    for (int t=0; t<10; t++) {
      threads[t].start();
    }

    try {
      for (int t=0; t<10; t++) {
        threads[t].join();
      }
    } catch (InterruptedException exn) { System.out.println("fucked"); }

    System.out.println(count.get());
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


class MyAtomicInteger {

  private int value = 0;
  public synchronized int addAndGet(int amount)
  {
    value += amount;
    return value;
  }

  public synchronized int get()
  {
    return value;
  }
}