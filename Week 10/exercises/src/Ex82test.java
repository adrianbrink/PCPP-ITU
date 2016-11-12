import java.lang.RuntimeException;
import java.lang.Thread;
//import java.util.Function;
import java.util.function.Consumer;

class Ex82test {

	

	public static void main(String[] args){

		it_should_not_be_able_to_take_a_read_lock_while_holding_a_write_lock();
		it_should_not_be_able_to_take_a_write_lock_while_holding_a_read_lock();
	 	it_should_not_be_allowed_to_unlock_a_read_lock_that_it_does_not_already_hold();
	 	it_should_not_be_allowed_to_unlock_a_write_lock_that_it_does_not_already_hold();
	 	it_unlocks_readlock();
	 	it_unlocks_writelock();

	 	// parallel
	 	it_removes_thread_from_readerList_when_unlocking();
	 	threads_should_not_be_able_to_take_a_read_lock_while_another_thread_holds_a_write_lock();
	}

	public static void it_unlocks_readlock()
	{
		SimpleRWTryLock lock = new SimpleRWTryLock();
		lock.readerTryLock();
		lock.readerUnlock();
		if ( lock.contains(Thread.currentThread()) != true) System.out.println( "Passed" + ": " + getMethodName(1));
	}

	public static void it_unlocks_writelock()
	{
		SimpleRWTryLock lock = new SimpleRWTryLock();
		lock.writerTryLock();
		lock.writerUnlock();
		if ( lock.holders.get() == null) System.out.println( "Passed" + ": " + getMethodName(1));
	}


	public static void it_should_not_be_able_to_take_a_read_lock_while_holding_a_write_lock() {
		SimpleRWTryLock lock = new SimpleRWTryLock();
		
		if( lock.writerTryLock() == true && lock.readerTryLock() == false)
			System.out.println( "Passed" + ": " + getMethodName(1));
	}

	public static void it_should_not_be_able_to_take_a_write_lock_while_holding_a_read_lock(){
		SimpleRWTryLock lock = new SimpleRWTryLock();

		if( lock.readerTryLock() == true && lock.writerTryLock() == false)
			System.out.println( "Passed" + ": " + getMethodName(1));
	}

	public static void it_should_not_be_allowed_to_unlock_a_read_lock_that_it_does_not_already_hold(){
		
		SimpleRWTryLock lock = new SimpleRWTryLock();
		
		try {
			lock.readerUnlock();	
		} catch (RuntimeException e) {
			System.out.println( "Passed" + ": " + getMethodName(1));
		}
	}

	public static void it_should_not_be_allowed_to_unlock_a_write_lock_that_it_does_not_already_hold(){

		SimpleRWTryLock lock = new SimpleRWTryLock();

		try {
			lock.writerUnlock();
		} catch (RuntimeException e) {
			System.out.println( "Passed" + ": " + getMethodName(1));
		}
	}

	public static void it_removes_thread_from_readerList_when_unlocking()
	{
		
		Consumer<DTO> action = dto -> {
			dto.lock.readerTryLock();
			if(dto.threadNr == 3) dto.lock.readerUnlock();	
		};

		Consumer<SimpleRWTryLock> checker = lock -> {
			ReaderList rl = (ReaderList) lock.holders.get();
			boolean errored = false;
			while(rl != null)
			{
				String threadName = rl.thread.getName();
				if (threadName == "Thread-3") errored = true;
				rl = rl.next;
			}
			String result = errored ? "Failed" : "Passed";
			System.out.println( result + ": " + getMethodName(1));
		};

		testInParallel(action, checker);
	}

	public static void threads_should_not_be_able_to_take_a_read_lock_while_another_thread_holds_a_write_lock()
	{
		Consumer<DTO> action = dto -> {
			if(dto.threadNr == 0) dto.lock.writerTryLock();	
			else dto.lock.readerTryLock();
		};

		Consumer<SimpleRWTryLock> checker = lock -> {
			boolean valid = (lock.holders.get() instanceof Writer);
			String result = valid ? "Passed" : "Failed";
			System.out.println( result + ": " + getMethodName(1));
		};

		testInParallel(action, checker);

	}	

	public static String getMethodName(final int depth)
	{
	  final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
	  return ste[ste.length - 1 - depth].getMethodName();
	}

	public static void testInParallel(Consumer<DTO> action, Consumer<SimpleRWTryLock> checker)
	{
		SimpleRWTryLock lock = new SimpleRWTryLock();
		int threadNr = 5;
		Thread[] threads = new Thread[threadNr];

		for (int i = 0; i <threadNr; i ++)
		{
			final int j = i;
			threads[i] = new Thread( () ->action.accept(new DTO(lock, j)) );
		}

		for (Thread t : threads){
			t.start();
		}
		try {
			for (Thread t : threads){
				t.join();
			}
		} catch(InterruptedException e ){}

		checker.accept(lock);
	}
}

class DTO {
	SimpleRWTryLock lock;
	int threadNr;

	public DTO(SimpleRWTryLock lock, int threadNr){
		this.lock = lock;
		this.threadNr = threadNr;
	}

}