public class PrinterTest {
  public static void main(String[] args) {
    System.out.print("Test");

    final Printer p = new Printer();

    Thread t1 = new Thread(() -> {
      while (true) {
        Printer.print();
      }
    });

    Thread t2 = new Thread(() -> {
      while (true) {
        Printer.print();
      }
    });

    t1.start();
    t2.start();
  }
}

class Printer {
  public void print() {
    synchronized (this) {
      System.out.print("=");
      try { Thread.sleep(50); } catch (InterruptedException exn) {}
      System.out.print("|");
    }
  }

  public static void print1() {
    synchronized (Printer.class) {
      System.out.print("=");
      try { Thread.sleep(50); } catch (InterruptedException exn) {}
      System.out.print("|");
    }
  }
}

