import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * A priority queue implementation with a fixed size based on a {@link priorityQueue}. The number of elements in the queue will be at most
 * {@code maxSize}. Once the number of elements in the queue reaches {@code maxSize}, trying to add a new element will remove the greatest
 * element in the queue if the new element is less than or equal to the current greatest element. The queue will not be modified otherwise.
 */
public class FixedSizeMaxPriorityQueue<E> extends PriorityQueue<E> {
    private static final long serialVersionUID = 1L;
    private final int maxSize;

    /**
     * Constructs a {@link FixedSizeMaxPriorityQueue} with the specified {@code maxSize} and {@code comparator}.
     *
     * @param maxSize
     *            - The maximum size the queue can reach, must be a positive integer.
     * @param comparator
     *            - The comparator to be used to compare the elements in the queue, must be non-null.
     */
    public FixedSizeMaxPriorityQueue(final int maxSize, final Comparator<? super E> comparator) {
        super(comparator); // reverse the comparator so we keep the max at the head.
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize = " + maxSize + "; expected a positive integer.");
        }
        this.maxSize = maxSize;
    }

    /**
     * Adds an element to the queue. If the queue contains {@code maxSize} elements, {@code e} will be compared to the lowest element in the
     * queue using {@code comparator}. If {@code e} is greater than or equal to the lowest element, that element will be removed and
     * {@code e} will be added instead. Otherwise, the queue will not be modified and {@code e} will not be added.
     *
     * @param e
     *            - Element to be added, must be non-null.
     * @return
     */
    @Override
    public boolean add(final E e) {
        if (e == null) {
            throw new NullPointerException("e is null.");
        }
        if (maxSize <= this.size()) {
            final E firstElm = this.peek();
            if (this.comparator().compare(e, firstElm) < 1) {
                return false;
            } else {
                this.poll();
            }
        }

        return super.add(e);
    }

    /**
     * @return Returns a sorted view of the queue as a {@link Collections#unmodifiableList(java.util.List)} unmodifiableList.
     */
    public List<E> asList() {
        return Collections.unmodifiableList(new ArrayList<E>(this));
    }
}