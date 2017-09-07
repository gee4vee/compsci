import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A solution to the classic problem using a Lock and Condition.
 * 
 * @author Gabriel Valencia, gee4vee@me.com
 */
public class DiningPhilosophers {
	
	/**
	 * A philosopher just thinks, eats, and then dies.
	 * 
	 * @author Gabriel Valencia, gee4vee@me.com
	 */
	private static class Philosopher implements Runnable {
		
		private static Random rand = new Random();
		
		private int id;
		private Chopstick leftChopstick;
		private Chopstick rightChopstick;
		private boolean fast = false;
		
		private volatile boolean alive = true;
		
		public Philosopher(int id) {
			this.id = id;
		}
		
		public Philosopher(int id, boolean fast) {
			this(id);
			this.fast = fast;
		}

		/**
		 * Thinks and eats repeatedly for a random lifespan until he/she dies.
		 */
		@Override
		public void run() {
			long start = System.currentTimeMillis();
			long lifespan;
			if (this.fast) {
				lifespan = rand.nextInt(120000) + 60000; // lifespan is 1-2 mins
			} else {
				lifespan = rand.nextInt(240000) + 60000; // lifespan is 1-4 mins
			}
			while (this.alive) {
				this.think();
				this.eat();
				long now = System.currentTimeMillis();
				if ((now - start) > lifespan) {
					this.die();
				}
			}
		}
		
		/**
		 * The philosopher pauses for some random length of time to contemplate the big questions of life.
		 */
		public void think() {
			System.out.println("Philosopher #" + this.id + ": I wonder...");
			try {
				if (!this.fast) {
					Thread.sleep(rand.nextInt(21000) + 1);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * The philosopher first indicates that he/she is hungry and then attempts to pick up both chopsticks. He/she will wait 
		 * until both are available. When they are, he/she picks them both up and eats for some time. Afterwards, the 
		 * chopsticks are both put down.
		 */
		public void eat() {
			System.out.println("Philosopher #" + this.id + ": All this thinking has made me HUNGRY!");
			this.leftChopstick.pickUp();
			this.rightChopstick.pickUp();
			System.out.println("Philosopher #" + this.id + ": This food is good!");
			try {
				if (!this.fast) {
					Thread.sleep(10000L);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Philosopher #" + this.id + ": I'm full for now!");
			this.leftChopstick.putDown();
			this.rightChopstick.putDown();
		}
		
		/**
		 * The philosopher dies and finally stops thinking and eating.
		 */
		public void die() {
			this.alive = false;
			System.out.println("Philosopher #" + this.id + ": Goodbye, cruel world! (✖╭╮✖)");
		}
		
	}
	
	/**
	 * Represents the classic eating utensil. Can be picked up and put down.
	 * 
	 * @author Gabriel Valencia, gee4vee@me.com
	 */
	private static class Chopstick {
		
		private final Lock chopstickLock = new ReentrantLock();
		private final Condition chopstickAvailable = this.chopstickLock.newCondition();
		
		private int id;
		private volatile boolean inUse = false;
		
		public Chopstick(int id) {
			this.id = id;
		}
		
		public int getId() {
			return this.id;
		}
		
		/**
		 * Called by a philosopher to reserve a chopstick before eating.
		 */
		public void pickUp() {
			this.chopstickLock.lock();
			try {
				if (this.inUse) {
					try {
						// chopstick is in use. the philosopher will wait (releasing the lock) until signaled in putDown().
						this.chopstickAvailable.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				// philosopher will reserve this chopstick.
				this.inUse = true;
			} finally {
				this.chopstickLock.unlock();
			}
		}
		
		/**
		 * Called by a philosopher to release a chopstick after eating.
		 */
		public void putDown() {
			this.chopstickLock.lock();
			try {
				this.inUse = false;
				// a philosopher waiting in pickUp() will be chosen at random to continue.
				this.chopstickAvailable.signalAll();
			} finally {
				this.chopstickLock.unlock();
			}
		}
	}
	
	/**
	 * Represents a circular table. The circular configuration promotes discussion amongst philosophers.
	 * 
	 * @author Gabriel Valencia, gee4vee@me.com
	 */
	private static class CircularTable {
		private List<Philosopher> diners = new ArrayList<>();
		
		/**
		 * @param fast If <code>true</code>, philosophers won't pause much, which is a more rigorous test for deadlocks. 
		 * Use <code>false</code> to follow the philosophers' actions more easily.
		 */
		public CircularTable(boolean fast) {
			Chopstick c1 = new Chopstick(1);
			Chopstick c2 = new Chopstick(2);
			Chopstick c3 = new Chopstick(3);
			Chopstick c4 = new Chopstick(4);
			Chopstick c5 = new Chopstick(5);
			
			Philosopher p1 = new Philosopher(1, fast);
			diners.add(p1);
			Philosopher p2 = new Philosopher(2, fast);
			diners.add(p2);
			Philosopher p3 = new Philosopher(3, fast);
			diners.add(p3);
			Philosopher p4 = new Philosopher(4, fast);
			diners.add(p4);
			Philosopher p5 = new Philosopher(5, fast);
			diners.add(p5);
			
			p1.rightChopstick = c1;
			p1.leftChopstick = c2;
			
			p2.rightChopstick = c2;
			p2.leftChopstick = c3;
			
			p3.rightChopstick = c3;
			p3.leftChopstick = c4;
			
			p4.rightChopstick = c4;
			p4.leftChopstick = c5;
			
			p5.rightChopstick = c5;
			p5.leftChopstick = c1;
		}
		
		/**
		 * Begins the lifecycle of all philosophers dining at this table.
		 */
		public void start() {
			for (Philosopher diner : this.diners) {
				Thread pThread = new Thread(diner);
				pThread.start();
			}
		}
		
		/**
		 * Immediately kills all philosophers dining at this table.
		 */
		public void stop() {
			for (Philosopher diner : this.diners) {
				diner.die();
			}
		}
	}

	/**
	 * Runs the dining philosopher simulation.
	 * 
	 * @param args Pass in <code>true</code> to enable fast mode for more rigorous deadlock testing.
	 */
	public static void main(String[] args) {
		boolean fast = false;
		if (args.length > 0) {
			String arg = args[0];
			fast = Boolean.parseBoolean(arg);
		}
		CircularTable table = new CircularTable(fast);
		table.start();
	}

}
