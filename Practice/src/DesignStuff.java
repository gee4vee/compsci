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
	 * Design and implement a class for a single-threaded unique ID pool. It must have the following functions:
	 * max -> long (maximum number of available IDs)
	 * alloc() -> id
	 * release(id)
	 * 
	 * Optimize solution for the worst case when all but one of the available IDs has been allocated.
	 * 
	 * @author Gabriel Valencia, <gee4vee@me.com>
	 */
	public static class IDSpace {
	    
	    // use a bit vector to track allocated IDs.
	    // to optimize the worst case, break up the vector in ranges by splitting in halfs. for each range, track whether 
	    // there is at least one available ID.
	}
	
	/**
	 * Design a class for a single-threaded transaction manager for a key-value store. It must have the following functions:
	 * begin() -> txid
	 * write(txid, String key, int value)
	 * read(txid, String key) -> int
	 * commit(txid)
	 * _abort(txid)
	 * 
	 * Assume it has access to the underlying persistent key-value via a client interface. Assume there is a function available 
	 * to allocate a unique transaction ID. The implementation must handle interleaving commits based on whichever client executes 
	 * commit() first and ensure that dirty reads abort the corresponding transaction. In other words, suppose transaction #1 modified 
	 * key A and transaction #2 also modified key A but transaction #2 calls commit() before transaction #1. In that scenario, 
	 * transaction #1 must be aborted.
	 * 
	 * @author Gabriel Valencia, <gee4vee@me.com>
	 */
	public static class TransactionManager {
	    // use a Map keyed by txid whose value is the state of key-value pairs for that transaction scope. for each value, 
	    // need to also associate it with the timestamp when the write() operation was called.
	    // read() will look in the transaction scope map first, and then look in the persistent store.
	    // write() will just write to the transaction scope map.
	    // commit() will check if any of the keys being modified have been changed in the persistent store since the timestamp 
	    // recorded in the transaction scope map. if so, the transaction needs to be aborted.
	    // _abort() will remove the transaction scope map from memory. optionally notify clients of aborted transactions.
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
