import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Problem taken from "Cracking the Coding Interview" by Gayle Laakmann McDowell. Demonstrates guaranteed order of execution 
 * using a synchronization block.
 * 
 * @author Gabriel Valencia, <gee4vee@me.com>
 */
public class FizzBuzzTest {
    
    public FizzBuzzTest(int n) {
    }
    
    public static class Counter {
        
        public final long n;
        public final AtomicLong counter;
        public final Queue<String> globalQueue = new ConcurrentLinkedQueue<>();
        
        public Counter(long n) {
            this.n = n;
            this.counter = new AtomicLong(1);
        }
        
        public long getCount() {
            return this.counter.get();
        }
        
        public long increment() {
            return this.counter.incrementAndGet();
        }
    }
    
    public static class FizzBuzzThread extends Thread {
        
        private final Counter count;
        private final Function<Long, String> op;
        private final Queue<String> queue = new LinkedList<>();
        
        public FizzBuzzThread(Counter count, Function<Long, String> op) {
            this.count = count;
            this.op = op;
        }
        
        public Queue<String> getQueue() {
            return this.queue;
        }
        
        @Override
        public void run() {
            while (true) {
                synchronized (this.count) {
                    long count = this.count.getCount();
                    if (count <= this.count.n) {
                        String result = this.op.apply(count);
                        if (result != null) {
                            this.queue.offer(result);
                            System.out.print(result);
                            this.count.increment();
                            this.count.globalQueue.offer(result);
                        }
                    } else {
                        break;
                    }
                }
            }
        }
    }
    
    public static void main(String[] args) {
        long n = 30;
        if (args.length > 0) {
            try {
                n = Long.parseLong(args[0]);
                if (n <= 0) {
                    System.err.println("Must enter non-negative integer.");
                    System.exit(1);
                }
            } catch (NumberFormatException e) {
                System.err.println("Must enter non-negative integer.");
                System.exit(1);
            }
        }
        
        // first calculate expected result sequentially;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= n; i++) {
            if (i % 3 == 0 && i % 5 == 0) {
                sb.append("FizzBuzz");
            } else if (i % 3 == 0) {
                sb.append("Fizz");
            } else if (i % 5 == 0) {
                sb.append("Buzz");
            } else {
                sb.append(i);
            }
        }
        String expected = sb.toString();
        System.out.println("Expected string: " + expected);
        System.out.print("  Actual string: ");
        
        Counter counter = new Counter(n);
        FizzBuzzThread printer = new FizzBuzzThread(counter, (input -> {
            if ((input % 3) != 0 && (input % 5) != 0) {
                return Long.toString(input);
            }
            return null;
        }));
        
        FizzBuzzThread div3 = new FizzBuzzThread(counter, (input -> {
            if ((input % 3) == 0 && (input % 5) != 0) {
                return "Fizz";
            };
            return null;
        }));
        
        FizzBuzzThread div5 = new FizzBuzzThread(counter, (input -> {
            if ((input % 5) == 0 && (input % 3) != 0) {
                return "Buzz";
            };
            return null;
        }));
        
        FizzBuzzThread div3And5 = new FizzBuzzThread(counter, (input -> {
            if ((input % 3) == 0 && (input % 5) == 0) {
                return "FizzBuzz";
            };
            return null;
        }));
        
        printer.start();
        div3.start();
        div5.start();
        div3And5.start();
        
        try {
            printer.join();
            div3.join();
            div5.join();
            div3And5.join();
            
            int count = 0;
            Queue<String> queue = printer.getQueue();
            while (!queue.isEmpty()) {
                long result = Long.parseLong(queue.poll());
                if ((result % 3 == 0) || result % 5 == 0) {
                    System.err.println("Test failed for printer for result " + result);
                    break;
                }
                count++;
            }
            
            queue = div3.getQueue();
            while (!queue.isEmpty()) {
                String result = queue.poll();
                if (!"Fizz".equals(result)) {
                    System.err.println("Test failed for div3 for result " + result);
                    break;
                }
                count++;
            }
            
            queue = div5.getQueue();
            while (!queue.isEmpty()) {
                String result = queue.poll();
                if (!"Buzz".equals(result)) {
                    System.err.println("Test failed for div5 for result " + result);
                    break;
                }
                count++;
            }
            
            queue = div3And5.getQueue();
            while (!queue.isEmpty()) {
                String result = queue.poll();
                if (!"FizzBuzz".equals(result)) {
                    System.err.println("Test failed for div3And5 for result " + result);
                    break;
                }
                count++;
            }
            
            if (count != n) {
                System.err.println("Test failed. Expected count of " + n + " but got " + count + ".");
            }
            
            sb = new StringBuilder();
            while (!counter.globalQueue.isEmpty()) {
                sb.append(counter.globalQueue.poll());
            }
            String actual = sb.toString();
            if (!expected.equals(actual)) {
                System.err.println("Test failed: unexpected string result: " + actual);
            }
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
    }
    
}
