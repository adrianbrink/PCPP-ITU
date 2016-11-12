
import java.util.concurrent.atomic.AtomicReference;
import java.lang.Thread;
import java.lang.RuntimeException;

class SimpleRWTryLock {

	AtomicReference<Holders> holders = new AtomicReference<Holders>();

	/*
	is called by a thread that tries to obtain a read lock.
	It must succeed and return true if the lock is held only
	by readers (or nobody), and return false if the lock is
	held by a writer.
	*/
	public boolean readerTryLock() {
		
		final Thread current = Thread.currentThread();
		
		Holders holder = holders.get();

		while(holder == null || holder instanceof ReaderList)
		{
			if(contains(current)) break;

			ReaderList readerList = new ReaderList(current, (ReaderList) holder);
		
			if(holders.compareAndSet(holder, readerList) == true ) return true;

			holder = holders.get();
		}

		return false;
	}

	/*
	is called to release a read lock, and must throw an exception
	if the calling thread does not hold a read lock.
	*/
	public void readerUnlock() {
		Holders holder = holders.get();
		Thread t = Thread.currentThread();

		if ( ! contains(t)) throw new RuntimeException("This thread doesn't have a read lock");

		while(holder != null && holder instanceof ReaderList && contains(t) )
		{
			holders.compareAndSet(holder, remove(t));
			holder = holders.get();
		}
	}

	public boolean contains(Thread t){
		if (holders.get() instanceof Writer) return false;
		ReaderList readerList = (ReaderList) holders.get();
		while(readerList != null){
			if( readerList.thread == t) return true;
			readerList = readerList.next;
		}
		return false;		
	}

	private ReaderList remove(Thread t){
		return createList( (ReaderList) holders.get(), t);
	}

	private ReaderList createList(ReaderList list, Thread t)
	{
		if (list == null) return null;
		if(list.thread == t) return createList(list.next, t);
		return new ReaderList(list.thread, createList(list.next, t));
	}

	/*
	It must succeed and return true if the lock is not 
	already held by any thread, and return false if the
	lock is held by at least one reader or by a writer. 
	*/
	public boolean writerTryLock() {
		final Thread current = Thread.currentThread();
		Writer writer = new Writer(current);
		return holders.compareAndSet(null, writer);
	}


	/*
	is called to release the write lock, and must throw
	an exception if the calling thread does not hold
	a write lock.
	*/
	public void writerUnlock() {
		
		Holders holder = holders.get();
		if( holder == null || !(holder instanceof Writer) ) throw new RuntimeException("there's no writer lock");
		if( ((Writer) holder).thread != Thread.currentThread()) throw new RuntimeException("This thread doesn't have a write lock");
		boolean result = holders.compareAndSet(holder, null);
	}
}

abstract class Holders { }

class ReaderList extends Holders {
	public final Thread thread;
	public final ReaderList next;

	public ReaderList(Thread thread, ReaderList next){
		this.thread = thread;
		this.next = next;
	}
}

class Writer extends Holders {

	public final Thread thread;

	public Writer(Thread thread) {
		this.thread = thread;
	}
	
}

