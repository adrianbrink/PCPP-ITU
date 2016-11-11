

public class Printer {
    public static void print() {
        
        synchronized(Printer.class) {
            System.out.print("-");
            
            try {
                Thread.sleep(50);
                
            } catch (InterruptedException e) {}
            System.out.print("|");
            
        }
        
    }
    
    public static void main(String[] args) {
        final Printer p = new Printer();
        
        Thread t1 = new Thread(() -> {
            while (true) Printer.print();
            
        });
        
        Thread t2 = new Thread(() -> {
            while (true) Printer.print();
        });
        
        t1.start();
        t2.start();
    }

}

