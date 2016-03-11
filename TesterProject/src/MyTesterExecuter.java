import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
public class MyTesterExecuter {

	public static void main(String[] args) {
		
		//-----simple one-------
		/*
		
		ExecutorService executer1 = Executors.newSingleThreadExecutor();
		executer1.execute(new WorkerThread(executer));
	
		System.out.println("Main thread ");
		
		 */
		
		//-----countdown latch-------
		/*ExecutorService executer = Executors.newFixedThreadPool(5);
		CountDownLatch doneSignal = new CountDownLatch(5);
		
		for(int i=0; i < 5; i++){
			executer.execute(new WorkerThread1(doneSignal, "Thraed "+String.valueOf(i)));
			
		}
		
		
		try {
			doneSignal.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Printed after all threads are empty");
		
		executer.shutdown();*/
		
		//Future
		//---callable--
		String[] argd={"abc","dkg","lbwe"};
		
		ExecutorService pool = Executors.newFixedThreadPool(3);
		Set<Future<Integer>> set = new HashSet<Future<Integer>>();
		for (String word: argd) {
		Callable<Integer> callable = new WordLengthCallable(word);
		Future<Integer> future = pool.submit(callable);
		set.add(future);
		}
		int sum = 0;
		for (Future<Integer> future : set) {
		try {
			sum += future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		System.out.printf("The sum of lengths is %s%n", sum);
		System.exit(sum);

	}
}

 class WordLengthCallable implements Callable<Integer> {
	private String word;

	public WordLengthCallable(String word) {
		this.word = word;
	}

	public Integer call() {
		return Integer.valueOf(word.length());
	}


}

class WorkerThread1 implements Runnable {
	private CountDownLatch doneSignal;
	private String name;

	public WorkerThread1(CountDownLatch signal, String name) {

		this.doneSignal = signal;
		this.name = name;
	}

	@Override
	public void run() {

		System.out.println("Worker Thread doing the job" + name);
		amazing();
		doneSignal.countDown();

	}

	private void amazing() {
		System.out.println("amazing " + name);

	}

}

class WorkerThread implements Runnable {
	private ExecutorService parentService;

	public WorkerThread(ExecutorService executer) {

		parentService = executer;
	}

	@Override
	public void run() {

		try {
			System.out.println("Worker Thread doing the job");
			amazing();
			// throw new NullPointerException();

			// System.out.println("Worker Thread doing the job");
		} finally {

			parentService.shutdown();
			System.out.println("finally");
		}

	}

	private void amazing() {
		System.out.println("amazing");

	}

}
