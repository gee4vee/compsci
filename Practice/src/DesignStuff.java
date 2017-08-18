import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 
 */

/**
 * @author Gabriel Valencia, gee4vee@me.com
 *
 */
public class DesignStuff {
	
	private static final long HIT_TRACKER_INTERVAL = 1000L * 60 * 1;
	
	private static AtomicLong lastHitTime = new AtomicLong(System.currentTimeMillis());
	
	// use a LongAdder for the counter because it performs better with high contention
	private static ConcurrentMap<String, ConcurrentHashMap<Long, LongAdder>> hitMaps = new ConcurrentHashMap<>();
	private static ConcurrentMap<String, SortedSet<Long>> hitTimestamps = new ConcurrentHashMap<>();
	
	public static void logHits(String page, int hits, long timestamp) {
		long lastHitTS = lastHitTime.get();
		long hitKey;
		if ((timestamp - lastHitTS) > HIT_TRACKER_INTERVAL) {
			hitKey = timestamp;
			lastHitTime.set(timestamp);
		} else {
			hitKey = lastHitTS;
		}
		hitTimestamps.computeIfAbsent(page, key -> new TreeSet<>()).add(hitKey);
		hitMaps.computeIfAbsent(page, key -> new ConcurrentHashMap<>())	// initialize the hit map for this page
		.computeIfAbsent(hitKey, key -> new LongAdder()).add(hits);	// initialize the counter for this timestamp and increment
	}
	
	public static long getHits(String page, long dateStart, long dateEnd) {
		long totalHits = 0L;
		SortedSet<Long> pageKeys = hitTimestamps.computeIfAbsent(page, key -> new TreeSet<>());
		pageKeys = pageKeys.tailSet(dateStart).headSet(dateEnd);
		ConcurrentHashMap<Long, LongAdder> hitCounters = hitMaps.computeIfAbsent(page, key -> new ConcurrentHashMap<>());
		for (Long key : pageKeys) {
			LongAdder count = hitCounters.get(key);
			totalHits += count.sum();
		}
		
		return totalHits;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
