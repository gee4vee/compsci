import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A basic node for building simple n-ary trees or simple unweighted graphs. Also supports discovering the lineage of a node.
 * 
 * @param <T> The type of the node's value, which must implement <code>Comparable</code>.
 * 
 * @author gvalenc
 */
public class NAryTreeNode<T extends Comparable<T>> implements Comparable<NAryTreeNode<T>>{
    
    /**
     * The delimiter used to separate nodes in the String format of a node's FQV.
     * 
     * @see #getFQVString()
     */
    public static final String FQV_DELIM = ".";
    
    /**
     * The FQV delimiter used in Java regular expressions.
     */
    private static final String FQV_DELIM_REGEX = "\\.";
    
    private T val;
    private final List<NAryTreeNode<T>> children = new ArrayList<>();
    private NAryTreeNode<T> parent = null;
    
    /**
     * performance testing insights:
     * with small trees (less than 10K nodes and 50 levels), the advantages of enabling the cache in node search times are generally negligible.
     * 
     * with trees containing over 100K nodes, ~150 levels, max 75 children per level, shows that node searches by value can be 
     * 2x-3x slower when caching is enabled. node searches by FQV are also often faster without caching. 
     * 
     * tree creation time can be an order of magnitude slower for large trees when enabling caching due to the overhead in calculating the 
     * lineage every time a node is added. the advantage begins to decline as the tree increases in size but not to the point of showing caching 
     * to be better. a major downside is that a tree with more than 300K nodes will exhaust a JVM with a 3GB heap when caching is 
     * enabled, assuming String node values of length ~12. with caching disabled, the required memory is an order of magnitude smaller.
     * 
     * it is likely that the benefits of caching with large trees will be more evident if the system is under high CPU and/or memory load. 
     * also, as we approach 300K nodes or more, the difference in node search times begins to shrink. it's possible that for very large trees, 
     * searches with caching enabled will be faster. nonetheless, for general usage the advantage isn't evident and so caching is disabled by default.
     */
    private boolean cacheLineage = false;
    
    /**
     * If this is <code>false</code>, we use a weak reference instead of a soft reference for the lineage cache. The default value is <code>false</code> 
     * because the overhead in determining if the referent is softly reachable becomes too noticeable as the tree size increases; large pauses 
     * to reclaim memory ensue with no noticeable increase in performance. This does not happen with weak references. Of course, the disadvantage 
     * with using a weak reference is that the caches are cleared more aggressively. Setting this to <code>true</code> will switch to using soft 
     * references.
     */
    private boolean useStrongerLineageCache = false;
    
    /**
     * A cache of the lineage values so we only calculate them when resetting the parent, i.e. when a node is added to a tree.
     */
    private String lineageValuesCache = null;
    
    /**
     * A cache of the lineage nodes so that we only calculate the lineage when resetting the parent. Using a reference ensures that we don't exhaust 
     * the heap with caches for nodes that are never accessed.
     */
    private Reference<Deque<NAryTreeNode<T>>> lineageNodesCache = this.createLineageCacheRef();
    
    public NAryTreeNode(T v) { 
        this.val = v;
    }
    
    /**
     * Returns the value of this node.
     */
    public T getValue() {
        return this.val;
    }
    
    /**
     * Sets the value of this node.
     * 
     * @param v The new value to set.
     * 
     * @return This node.
     */
    public NAryTreeNode<T> setValue(T v) {
        this.val = v;
        return this;
    }
    
    /**
     * Sets whether the lineage cache will be enabled for this node. The lineage cache will be populated initially when the node is added to 
     * a tree and might be recalculated if memory demands on the JVM cause the cache to be cleared. By default the lineage cache is disabled 
     * as it is only beneficial for very large trees in a system experiencing heavy load.
     * 
     * @param toggle If <code>true</code>, the lineage cache will be enabled; otherwise it will be disabled.
     * 
     * @return This node.
     */
    public NAryTreeNode<T> toggleLineageCache(boolean toggle) {
        this.cacheLineage = toggle;
        return this;
    }
    
    /**
     * Sets whether a stronger lineage cache is used to avoid losing lineage caches too quickly as memory demands increase. The downside of 
     * this is that overall GC performance can decrease as the GC tries to determine if lineage caches are available for clearing. By default 
     * the stronger lineage cache is disabled.
     * 
     * @param toggle If <code>true</code>, the stronger lineage cache will be enabled; otherwise it will be disabled.
     * 
     * @return This node.
     */
    public NAryTreeNode<T> toggleStrongerLineageCache(boolean toggle) {
        this.useStrongerLineageCache = toggle;
        return this;
    }
    
    /**
     * Returns the parent node of this node, or <code>null</code> if the node has no parent, i.e. it is the root of a tree.
     */
    public NAryTreeNode<T> getParent() {
        return this.parent;
    }
    
    /**
     * Sets the parent of this node. Client applications should <b>not</b> use this method.
     * 
     * @param p The new parent of this node.
     * 
     * @return This node.
     */
    NAryTreeNode<T> setParent(NAryTreeNode<T> p) {
        this.parent = p;
        if (this.cacheLineage) {
            this.lineageNodesCache.clear();
            this.lineageValuesCache = null;
            this.populateLineageCache();
        }
        
        return this;
    }
    
    /**
     * Returns the child nodes of this node. The returned list cannot be modified.
     */
    public List<NAryTreeNode<T>> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    /**
     * Adds the specified node as a child of this node.
     * 
     * @param n The node to add.
     * 
     * @return This node.
     */
    public NAryTreeNode<T> addChild(NAryTreeNode<T> n) {
        this.children.add(n);
        n.setParent(this);
        return this;
    }
    
    /**
     * Removes the child at the specified index. Note that this can cause the removal of an entire subtree.
     * 
     * @param index The index of the child to remove.
     * 
     * @return The removed node. The node's parent node will remain set to allow client applications to discover its former lineage. If this 
     * node is added back to a tree, it's parent node will be reset to the new parent.
     */
    public NAryTreeNode<T> removeChild(int index) {
        NAryTreeNode<T> removed = this.children.remove(index);
        return removed;
    }
    
    public NAryTreeNode<T> removeAllChildren() {
        this.children.clear();
        return this;
    }
    
    /**
     * Removes the child node with the specified value, if it exists. Note that this can cause the removal of an entire subtree.
     * 
     * @param value The value of the node to remove.
     * 
     * @return The removed node, or <code>null</code> if no such node exists. If an actual node is returned, its parent 
     * node will remain set to allow client applications to discover its former lineage. If this node is added back to a 
     * tree, it's parent node will be reset to the new parent.
     */
    public NAryTreeNode<T> removeChild(String value) {
        NAryTreeNode<T> removed = null;
        Iterator<NAryTreeNode<T>> itr = this.children.iterator();
        while (itr.hasNext()) {
            NAryTreeNode<T> n = itr.next();
            if (value == null) {
                if (n.val == null) {
                    itr.remove();
                    removed = n;
                    break;
                }
            } else {
                if (value.equals(n.val)) {
                    itr.remove();
                    removed = n;
                    break;
                }
            }
        }
        
        return removed;
    }
    
    /**
     * Returns whether this node has any children.
     */
    public boolean hasChildren() {
        return this.getNumChildren() > 0;
    }
    
    public boolean hasChild(NAryTreeNode<T> n) {
        for (NAryTreeNode<T> child : this.children) {
            if (child == n) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns the number of children in this node.
     */
    public int getNumChildren() {
        return this.children.size();
    }

    /**
     * Returns the values of all children of this node in the order returned by {@link #getChildren()}.
     */
    public List<T> getChildrenValues() {
        List<T> result = new ArrayList<>();
        this.children.forEach((node -> {
            result.add(node.val);
        }));
        
        return result;
    }
    
    /**
     * Returns the node with the specified value if it is a child of this node.
     * 
     * @param value The value to look for.
     * 
     * @return The child node with the specified value, or <code>null</code> if no such node exists.
     */
    public NAryTreeNode<T> getChildWithValue(String value) {
        for (NAryTreeNode<T> n : this.children) {
            if (value == null) {
                if (n.val == null) {
                    return n;
                }
            } else {
                if (value.equals(n.val)) {
                    return n;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Returns the fully qualified value (FQV) as an array where all elements except the last form the lineage of this node, starting from 
     * the root of its hierarchy. The last element is this node's value. If this is a root node, a single-element array containing this node's 
     * value will be returned.
     *  
     * @param clazz This must be the class of this node's type parameter. This parameter is necessary due to type erasure in Java.
     */
    public T[] getFQV(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz cannot be null");
        }
        
        if (this.parent == null) {
            @SuppressWarnings("unchecked")
            T[] arr = (T[]) Array.newInstance(clazz, 1);
            arr[0] = this.val;
            return arr;
        }
        
        Deque<NAryTreeNode<T>> lineageNodesStack = this.getLineageNodesStack();
        @SuppressWarnings("unchecked")
        T[] arr = (T[]) Array.newInstance(clazz, lineageNodesStack.size());
        for (int i = 0; i < arr.length; i++) {
            NAryTreeNode<T> first = lineageNodesStack.removeFirst();
            arr[i] = first.val;
        }
        
        return arr;
    }
    
    /**
     * Returns the fully qualified value (FQV) of this node, which is this node's value as a String prefixed by the values of lineage nodes 
     * as Strings using {@link #FQV_DELIM} as the delimiter between nodes. A root node's FQV is simply the node's value as a String.<br/>
     * <br/>
     * Note that this method relies on the {@link #toString()} implementation of this node's value. In order to produce meaningful FQV strings, 
     * the {@link #toString()} method of this node's type parameter class might need to be overridden for classes that rely on the default 
     * {@link Object#toString()} implementation or some other implementation that returns complex debugging information, for example.
     * 
     * @see #getLineageString()
     */
    public String getFQVString() {
        if (this.parent == null) {
            return this.val.toString();
        }
        
        String lineageStr = this.getLineageString();
        StringBuilder sb = new StringBuilder(lineageStr);
        if (!lineageStr.isEmpty()) {
            sb.append(FQV_DELIM);
        }
        sb.append(this.val);
        return sb.toString();
    }
    
    /**
     * Splits a FQV into an array consisting of its components.
     * 
     * @param fqv A FQV returned by {@link #getFQVString()}
     */
    public static String[] getSplitFQV(String fqv) {
        return fqv.split(FQV_DELIM_REGEX);
    }
    
    /**
     * Converts the specified array-format FQV into its String format using {@link #FQV_DELIM} as the delimiter.
     * 
     * @param fqv An array-format FQV, such as one returned by {@link #getFQV(Class)}.
     * 
     * @see #getFQVString()
     */
    public static <T> String getStringFQV(T[] fqv) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fqv.length; i++) {
            T val = fqv[i];
            sb.append(val.toString());
            if (i+1 < fqv.length) {
                sb.append(FQV_DELIM);
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Returns this node's lineage, i.e. all the parent nodes leading up to the hierarchy root, as a String using {@link #FQV_DELIM} as the 
     * delimiter between nodes, beginning with the root of the tree that contains this node. 
     */
    public String getLineageString() {
        if (this.cacheLineage) {
            return this.lineageValuesCache;
        }
        
        Deque<NAryTreeNode<T>> nodes = getLineageNodesStack();
        String lineageValues = convertToString(nodes);
        return lineageValues;
    }

    /**
     * Given a hierarchical lineage in String format, e.g. PSI.SSN, returns if this node's string-format lineage matches.
     * 
     * @param lineage A lineage of node values delimited by {@link #FQV_DELIM}.
     * 
     * @see #getLineageString()
     */
    public boolean lineageMatches(String lineage) {
        return this.getLineageString().equals(lineage);
    }
    
    /**
     * Returns an array view of the values of lineage nodes in order from hierarchy root to parent. If this node is a root, returns an empty array.
     *  
     * @param clazz This must be the class of this node's type parameter. This parameter is necessary due to type erasure in Java.
     */
    public T[] getLineage(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz cannot be null");
        }
        
        if (this.parent == null) {
            @SuppressWarnings("unchecked")
            T[] arr = (T[]) Array.newInstance(clazz, 0);
            return arr;
        }
        
        T[] fqv = this.getFQV(clazz);
        @SuppressWarnings("unchecked")
        T[] arr = (T[]) Array.newInstance(clazz, fqv.length-1);
        for (int i = 0; i < (fqv.length-1); i++) { // get everything except the last element, which is this node's value.
            arr[i] = fqv[i];
        }
        
        return arr;
    }

    /**
     * Given a lineage in array format, e.g. {PSI,SSN}, returns if this node's lineage matches.
     * 
     * @param lineage A lineage of node values in array format.
     * @param clazz This must be the class of this node's type parameter. This parameter is necessary due to type erasure in Java.
     * 
     * @see #getLineage(Class)
     */
    public boolean lineageMatches(T[] lineage, Class<T> clazz) {
        if (lineage == null) {
            throw new IllegalArgumentException("lineage cannot be null");
        }
        
        T[] thisL = this.getLineage(clazz);
        if (thisL.length != lineage.length) {
            return false;
        }
        
        boolean matches = true;
        for (int i = 0; i < thisL.length; i++) {
            T val = thisL[i];
            T otherVal = lineage[i];
            if (val == null && otherVal != null) {
                matches = false;
                break;
            }
            if (!val.equals(otherVal)) {
                matches = false;
                break;
            }
        }
        
        return matches;
    }
    
    /**
     * Returns a deque view of the lineage nodes in order from hierarchy root as the first element to parent as the last element.
     */
    public Deque<NAryTreeNode<T>> getLineageNodes() {
        return this.getLineageNodesStack();
    }

    private Deque<NAryTreeNode<T>> getLineageNodesStack() {
        if (this.cacheLineage) {
            if (this.lineageNodesCache.get() == null) {
                return this.populateLineageCache();
            }
            return this.lineageNodesCache.get();
        }
        
        Deque<NAryTreeNode<T>> nodes = buildLineageNodes(null, false);
        return nodes;
    }
    
    private Reference<Deque<NAryTreeNode<T>>> createLineageCacheRef() {
        if (this.useStrongerLineageCache) {
            /* 
             * a soft reference generally retains its referent in-memory longer than a weak reference because some determination is made to 
             * see if the referent is softly reachable.
             */
            return new SoftReference<Deque<NAryTreeNode<T>>>(new LinkedList<>());
        } else {
            return new WeakReference<Deque<NAryTreeNode<T>>>(new LinkedList<>());
        }
    }
    
    private Deque<NAryTreeNode<T>> populateLineageCache() {
        this.lineageNodesCache = this.createLineageCacheRef();
        Deque<NAryTreeNode<T>> localRef = this.lineageNodesCache.get(); // keep a local ref to make sure we don't lose it because of GC.
        Deque<NAryTreeNode<T>> copy = buildLineageNodes(localRef, true);
        // build the lineage values cache using the copy.
        this.lineageValuesCache = convertToString(copy);
        return copy;
    }

    private Deque<NAryTreeNode<T>> buildLineageNodes(Deque<NAryTreeNode<T>> lineageNodes, boolean returnCopy) {
        if (lineageNodes == null) {
            lineageNodes = new LinkedList<>();
        }
        
        Deque<NAryTreeNode<T>> copy = null; 
        if (returnCopy) {
            copy = new LinkedList<>();
        }
        NAryTreeNode<T> current = this.parent;
        while (current != null) {
            lineageNodes.addFirst(current);
            if (copy != null) {
                copy.addFirst(current);
            }
            current = current.parent;
        }
        
        return copy != null ? copy : lineageNodes;
    }
    
    private static <T extends Comparable<T>> String convertToString(Deque<NAryTreeNode<T>> lineageNodes) {
        StringBuilder sb = new StringBuilder();
        while (!lineageNodes.isEmpty()) {
            NAryTreeNode<T> n = lineageNodes.removeFirst();
            sb.append(n.val);
            if (!lineageNodes.isEmpty()) {
                sb.append(FQV_DELIM);
            }
        }
        return sb.toString();
    }
    
    /**
     * Returns the node in the specified collection that contains the specified value, or <code>null</code> if no such node exists.
     * 
     * @param nodes The nodes to check. Children of these nodes will not be checked.
     * @param value The node value to look for.
     */
    public static <T extends Comparable<T>> NAryTreeNode<T> findNodeWithValue(Collection<NAryTreeNode<T>> nodes, T value) {
        for (NAryTreeNode<T> n : nodes) {
            if (value == null) {
                if (n.val == null) {
                    return n;
                }
            } else {
                if (value.equals(n.val)) {
                    return n;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Returns the node that matches the specified FQV if it exists in the tree with the specified root.
     * 
     * @param root The root of a tree.
     * @param fqv The array-format FQV of a node. See {@link #getFQV(Class)}.
     * 
     * @return The node that matches the specified FQV or <code>null</code> if no such node exists.
     */
    public static <T extends Comparable<T>> NAryTreeNode<T> findNodeWithValue(NAryTreeNode<T> root, T[] fqv) {
        // since the value gives us the complete hierarchy, we can more quickly search each component directly.
        NAryTreeNode<T> compSearch = root;
        if (fqv != null) {
            for (int i = 0; i < fqv.length; i++) {
                T comp = fqv[i];
                compSearch = findNodeWithValue(compSearch, comp);
                if (compSearch == null) {
                    break;
                }
            }
            
            if (compSearch != null) {
                return compSearch; // we found it!
            }
        }
        
        return null;
    }

    /**
     * Returns the node in the tree with the specified root that contains the specified value, or <code>null</code> if 
     * no such node exists. If more than one such nodes exists, the node nearest to the specified root will be returned.
     * 
     * @param root A root node of a tree. The entire tree will be searched until the node is found or all nodes have been 
     * visited once.
     * @param value The node value to look for. Note that string-format FQVs are not supported.
     * 
     * @return The nearest node that contains the specified value or <code>null</code> if no such node exists.
     */
    public static <T extends Comparable<T>> NAryTreeNode<T> findNodeWithValue(NAryTreeNode<T> root, T value) {
        NAryTreeNode<T> result = null;
        if (value == null) {
            if (root.val == null) {
                result = root;
            }
        } else {
            if (value.equals(root.val)) {
                result = root;
            }
        }
        
        // BFS using queue, otherwise we could waste time going down a deep subtree when the expected node is among the 
        // children of the first node or elsewhere nearby.
        Queue<NAryTreeNode<T>> q = new LinkedList<>();
        q.offer(root);
        while (!q.isEmpty()) {
            int childCount = q.size();
            List<NAryTreeNode<T>> levelNodes = new ArrayList<>();
            while (childCount > 0) {
                NAryTreeNode<T> next = q.poll();
                levelNodes.add(next);
                for (NAryTreeNode<T> child : next.children) {
                    q.offer(child);
                }
                childCount--;
            }
            
            boolean targetFound = false;
            for (int i = 0; i < levelNodes.size(); i++) {
                NAryTreeNode<T> levelNode = levelNodes.get(i);
                if (value == null) {
                    if (levelNode.val == null) {
                        result = levelNode;
                        targetFound = true;
                        break;
                    }
                } else {
                    if (value.equals(levelNode.val)) {
                        result = levelNode;
                        targetFound = true;
                        break;
                    }
                }
            }
            
            if (targetFound) {
                break;
            }
        }
        
        return result;
    }

    /**
     * Builds trees from the specified properties object containing hierarchical property names separated by {@link NAryTreeNode#FQV_DELIM}. 
     * Note that only trees with String values are supported.
     * 
     * @param props A properties object.
     * 
     * @return A sorted set of roots of trees for all hierarchies discovered in the specified properties object. The tree nodes will <b>not</b> 
     * have their lineages cached.
     */
    public static SortedSet<NAryTreeNode<String>> extractHierarchies(Properties props) {
        return extractHierarchies(props, false);
    }
    
    /**
     * Builds trees from the specified properties object containing hierarchical property names separated by {@link NAryTreeNode#FQV_DELIM}. 
     * Note that only trees with String values are supported.
     * 
     * @param props A properties object.
     * @param cacheLineage If <code>true</code>, the tree nodes will cache their lineage as they are created. For most hierarchies, it is 
     * recommended to pass <code>false</code> as the cache requires memory that increases with the number of nodes in the trees and the 
     * performance benefits are only apparent for very large trees.
     * 
     * @return A sorted set of roots of trees for all hierarchies discovered in the specified properties object.
     */
    public static SortedSet<NAryTreeNode<String>> extractHierarchies(Properties props, boolean cacheLineage) {
        // each will contain a root of a graph.
        SortedSet<NAryTreeNode<String>> roots = new TreeSet<>();
        Set<String> searchTerms = props.stringPropertyNames();
        for (String searchTerm : searchTerms) {
            // e.g. PSI.SSN.GermanSSN
            String[] terms = searchTerm.split("\\.");
            for (int i = 0; i < terms.length; i++) {
                String term = terms[i];
                // search each hierarchy for the value.
                NAryTreeNode<String> childWithValue = null;
                for (NAryTreeNode<String> root : roots) {
                    NAryTreeNode<String> node = findNodeWithValue(root, term);
                    if (node != null) {
                        childWithValue = node;
                        break;
                    }
                }
                
                // if we didn't find it in any hierarchy, add it as a new hierarchy.
                if (childWithValue == null) {
                    childWithValue = new NAryTreeNode<String>(term);
                    roots.add(childWithValue);
                }
                
                // add the next term as a child of this one.
                if (i+1 < terms.length) {
                    String childT = terms[i+1];
                    if (childWithValue.getChildWithValue(childT) == null) {
                        NAryTreeNode<String> child = new NAryTreeNode<String>(childT);
                        childWithValue.addChild(child);
                    }
                }
            }
        }
        
        return roots;
    }
    
    /**
     * Returns a String representation of this node including its value.
     */
    @Override
    public String toString() {
        return NAryTreeNode.class.getSimpleName() + "["+this.val+"]";
    }
    
    /**
     * Returns a String representation of this node with the option of including more information.
     * 
     * @param debug If <code>true</code>, the returned String will contain the string-format FQV of this node as well as the number of 
     * children in this node. Otherwise, it will return the value returned by {@link #toString()}.
     * 
     * @see #getFQVString()
     */
    public String toString(boolean debug) {
        if (!debug) {
            return this.toString();
        }
        
        return NAryTreeNode.class.getSimpleName() + "["+this.getFQVString() + ", childCount=" + this.children.size() + "]";
    }

    /**
     * Compares this node's value with the value of the specified node.
     */
    @Override
    public int compareTo(NAryTreeNode<T> o) {
        return this.val.compareTo(o.val);
    }

    public static <T extends Comparable<T>> void printTree(NAryTreeNode<T> root) {
        printTree(root, false);
    }

    public static <T extends Comparable<T>> void printTree(NAryTreeNode<T> root, boolean withLineage) {
        Queue<NAryTreeNode<T>> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            int childCount = queue.size();
    
            while (childCount > 0) {
                NAryTreeNode<T> n = queue.poll();
                for (NAryTreeNode<T> c : n.children) {
                    queue.add(c);
                }
                if (withLineage) {
                    System.out.print(n.getFQVString() + " ");
                } else {
                    System.out.print(n.getValue() + " ");
                }
                childCount--;
            }
            if (childCount == 0) {
                System.out.println("");
            }
        }
    }
    
    
}
