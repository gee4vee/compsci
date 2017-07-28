import java.util.HashSet;
import java.util.Set;

public class ListStuff {
    public static void main(String[] args) {
        
    }

    public static Node detectCycle(Node a) {
        Node fast = a;
        Node slow = a;
        boolean hasCycle = false;
        while (fast != null && fast.right != null && slow != null) {
            fast = fast.right.right;
            slow = slow.right;
            if (fast == slow) {
                hasCycle = true;
                break;
            }
        }
        
        if (!hasCycle) return null;
        else {
            fast = a;
            while(fast != slow){
                fast = fast.right;
                slow = slow.right;
            }
            return fast;
        }
    }

    public static Node deleteDuplicates(Node a) {
        if (a == null) return null; // empty list
        if (a.right == null) return a; // one item list
        
        Set<String> vals = new HashSet<>();
        vals.add(a.value);
        Node current = a;
        Node next = a.right;
        while (current != null && next != null) {
            while (next != null && !vals.add(next.value)) { // keep going next until we don't find a dup.
                next = next.right;
            }
            current.right = next; // now we found the non-dup next of current.
            if (next == null) {
                break; // no more nodes left, so we're done.
            }
            current = next;
            next = current.right;
        }
        
        return a; // since nodes are sorted, the first node will always remain.
    }
}
