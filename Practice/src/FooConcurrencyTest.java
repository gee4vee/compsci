import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Problem taken from "Cracking the Coding Interview" by Gayle Laakmann McDowell. Demonstrates guaranteeing order of execution 
 * among multiple threads using a semaphore.
 * 
 * @author Gabriel Valencia, <gee4vee@me.com>
 */
public class FooConcurrencyTest {
    
    public static class Foo {
        
        private final Semaphore s1 = new Semaphore(1);
        private final Semaphore s2 = new Semaphore(1);
        
        /**
         * Used to record the order of execution of the methods. We won't want a concurrent queue here so that if our solution 
         * fails and the threads are not in the expected order, we will get a different order here.
         */
        private final Queue<String> queue = new LinkedList<>();
        
        public Foo() {
            try {
                // this locks all the Foo methods.
                this.s1.acquire();
                this.s2.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        public Queue<String> getQueue() {
            return this.queue;
        }
        
        public void first() {
            // this method doesn't acquire any permit from the semaphores while the other methods do, guaranteeing that it goes first.
            String result = "1";
            this.queue.offer(result);
            // let second() proceed.
            this.s1.release();
        }
        
        public void second() {
            // this method acquires a permit from the first semaphore, which is the only one released by first(), 
            // guaranteeing that it will go immediately after first().
            try {
                this.s1.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String result = "2";
            this.queue.offer(result);
            // this will allow third() to proceed.
            this.s2.release();
        }
        
        public void third() {
            // this method waits on the 2nd semaphore, which is only released by second().
            try {
                this.s2.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String result = "3";
            this.queue.offer(result);
        }
    }
    
    public static class FooRunnerA implements Runnable {
        
        private Foo foo;
        
        public FooRunnerA(Foo foo) {
            this.foo = foo;
        }
        
        @Override
        public void run() {
            this.foo.first();
        }
    }
    
    public static class FooRunnerB implements Runnable {
        
        private Foo foo;
        
        public FooRunnerB(Foo foo) {
            this.foo = foo;
        }
        
        @Override
        public void run() {
            this.foo.second();
        }
    }
    
    public static class FooRunnerC implements Runnable {
        
        private Foo foo;
        
        public FooRunnerC(Foo foo) {
            this.foo = foo;
        }
        
        @Override
        public void run() {
            this.foo.third();
        }
    }

    /**
     * @param args Enter a non-negative, non-zero integer for the number of iterations to test followed by the number of test threads.
     */
    public static void main(String[] args) {
        int numIterations = 1000000;
        int numTestThreads = 10;
        if (args.length > 0) {
            try {
                numIterations = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Must enter an integer > 0.");
            }
            
            if (args.length > 1) {
                try {
                    numTestThreads = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Must enter an integer > 0.");
                }
            }
        }
        
        // run it for many iterations and check the queue each time for consistent output.
        System.out.println("Running Foo concurrency test with " + numTestThreads + " test threads and "
                            + NumberFormat.getInstance().format(numIterations) + " iterations per thread...");
        long end = 0;
        SimpleDateFormat df = new SimpleDateFormat("MM/kk/yyyy hh:mm:ss aa");
        long start = System.currentTimeMillis();
        System.out.println("Test start time: " + df.format(new Date(start)));
        try {
            List<FooTester> testThreads = new ArrayList<>();
            for (int i = 0; i < numTestThreads; i++) {
                FooTester tester = new FooTester(i, numIterations);
                tester.start();
                testThreads.add(tester);
            }
            for (FooTester testThread : testThreads) {
                testThread.join();
                System.out.println("Test thread #" + testThread.getTesterId() + " has finished.");
                Exception testerException = testThread.getException();
                if (testerException != null) {
                    throw testerException;
                }
            }
            end = System.currentTimeMillis();
            System.out.print("Foo concurrency test passed!");
        } catch (Exception e) {
            end = System.currentTimeMillis();
            System.err.print("Foo concurrency test failed: " + e.getMessage());
        } finally {
            System.out.println(" Test end time: " + df.format(new Date(end)) + ".");
            long elapsed = (end - start);
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumIntegerDigits(2);
            long hours = TimeUnit.MILLISECONDS.toHours(elapsed);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsed) - 
            TimeUnit.MINUTES.toSeconds(minutes);
            String elapsedStr = String.format("%s:%s:%s", 
                    nf.format(hours),
                    nf.format(minutes),
                    nf.format(seconds)
                );
            System.out.println("Test elapsed time: " + elapsedStr + ".");
        }
    }
    
    public static class FooTester extends Thread {
        
        private final int testerId;
        private final int numIterations;
        private Exception err = null;
        
        public FooTester(int id, int numIterations) {
            this.testerId = id;
            this.numIterations = numIterations;
        }
        
        public int getTesterId() {
            return this.testerId;
        }
        
        public Exception getException() {
            return this.err;
        }

        @Override
        public void run() {
            try {
                runFooTest(this.numIterations);
            } catch (Exception e) {
                this.err = new Exception("Tester #" + this.testerId + " failed: " + e.getMessage(), e);
            }
        }
        
    }

    private static void runFooTest(int numIterations) throws Exception {
        for (int i = 0; i < numIterations; i++) {
            Foo foo = new Foo();
            Thread a = new Thread(new FooRunnerA(foo));
            Thread b = new Thread(new FooRunnerB(foo));
            Thread c = new Thread(new FooRunnerC(foo));
            a.start();
            b.start();
            c.start();
            // wait for all the threads to finish before checking the queue.
            a.join();
            b.join();
            c.join();
            
            Queue<String> queue = foo.getQueue();
            StringBuilder sb = new StringBuilder();
            while (!queue.isEmpty()) {
                sb.append(queue.poll());
            }
            String result = sb.toString();
            if (!"123".equals(result)) {
                throw new Exception("Fail! result=" + result);
            }
        }
    }

}
