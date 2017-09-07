import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophers {
	
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
		
		public void think() {
			try {
				System.out.println("Philosopher " + this.id + " is thinking...");
				if (!this.fast) {
					Thread.sleep(rand.nextInt(21000) + 1);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public void eat() {
			System.out.println("Philosopher " + this.id + " is hungry!");
			this.leftChopstick.pickUp();
			this.rightChopstick.pickUp();
			System.out.println("Philosopher " + this.id + " is eating...");
			try {
				if (!this.fast) {
					Thread.sleep(10000L);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Philosopher " + this.id + " finished eating!");
			this.leftChopstick.putDown();
			this.rightChopstick.putDown();
		}
		
		public void die() {
			this.alive = false;
			System.out.println("Philosopher " + this.id + " has DIED! :-(");
		}
		
	}
	
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
		
		public void pickUp() {
			this.chopstickLock.lock();
			try {
				if (this.inUse) {
					try {
						this.chopstickAvailable.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				this.inUse = true;
			} finally {
				this.chopstickLock.unlock();
			}
		}
		
		public void putDown() {
			this.chopstickLock.lock();
			try {
				this.inUse = false;
				this.chopstickAvailable.signalAll();
			} finally {
				this.chopstickLock.unlock();
			}
		}
	}
	
	private static class CircularTable {
		private List<Philosopher> diners = new ArrayList<>();
		
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
		
		public void start() {
			for (Philosopher diner : this.diners) {
				Thread pThread = new Thread(diner);
				pThread.start();
			}
		}
		
		public void stop() {
			for (Philosopher diner : this.diners) {
				diner.die();
			}
		}
	}

	public static void main(String[] args) {
		CircularTable table = new CircularTable(true);
		table.start();
	}

}
