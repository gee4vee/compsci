import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class TreeStuff {
	
	private static Random rand = new Random();

    public static void printTree(BinaryTreeNode root) {
		Queue<BinaryTreeNode> queue = new LinkedList<>();
		queue.offer(root);
		while (!queue.isEmpty()) {
            int childCount = queue.size();

            while (childCount > 0) {
                BinaryTreeNode n = queue.poll();
                if (n.left != null) {
                    queue.add(n.left);
                }
                if (n.right != null) {
                    queue.add(n.right);
                }
                System.out.print(n.val);
                if (n.peer != null) {
                    System.out.print("->" + n.peer.val + " ");
                } else {
                    System.out.print(" ");
                }
                childCount--;
            }
			if (childCount == 0) {
			    System.out.println("");
			}
		}
	}
    
    public static List<List<Integer>> levelOrder(BinaryTreeNode root) {
	    List<List<Integer>> result = new ArrayList<>();
	    Queue<BinaryTreeNode> queue = new LinkedList<>();
	    queue.offer(root);
        while (!queue.isEmpty()) {
            int childCount = queue.size();

            List<Integer> currentLevel = new ArrayList<>();
            while (childCount > 0) {
                BinaryTreeNode n = queue.poll();
                if (n.left != null) {
                    queue.add(n.left);
                }
                if (n.right != null) {
                    queue.add(n.right);
                }
                currentLevel.add(n.val);
                childCount--;
            }
            if (childCount == 0) {
                result.add(currentLevel);
            }
        }
        
        return result;
	}
    
	/**
	 * Link together nodes on the same level.
	 * 
	 * @param root
	 */
    public static void linkPeers(BinaryTreeNode root) {
        Queue<BinaryTreeNode> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            int childCount = queue.size();

            List<BinaryTreeNode> currentLevel = new ArrayList<>();
            while (childCount > 0) {
                BinaryTreeNode n = queue.poll();
                if (n.left != null) {
                    queue.add(n.left);
                }
                if (n.right != null) {
                    queue.add(n.right);
                }
                currentLevel.add(n);
                childCount--;
            }
            
            for (int i = 0; i < currentLevel.size(); i++) {
                BinaryTreeNode n = currentLevel.get(i);
                if (i+1 < currentLevel.size()) {
                    n.peer = currentLevel.get(i+1);
                }
            }
        }
    }
    
    /**
     * 
     *            1
     *        /       \
     *       3         5
     *      / \       / \
     *     8   2     9   7
     *    /
     *   10
     *   
     *   convert to:
     *   
     *   10 <-> 8 <-> 3 <-> 2 <-> 1 <-> 9 <-> 5 <-> 7
     *   10 <-> 7
     *   return 10
     *   
     * @param root
     * @return
     */
    public static BinaryTreeNode convertToOrderedList(BinaryTreeNode root, boolean cycle) {
        if (root == null) {
            return null;
        }
        
        BinaryTreeNode asList = inOrderList(root);
        
        BinaryTreeNode head = asList;
        BinaryTreeNode tail = asList;
        while (head.left != null) {
            head = head.left;
        }
        if (cycle) {
            while (tail.right != null) {
                tail = tail.right;
            }
            head.left = tail;
            tail.right = head;
        }
        
        return head;
    }
    
    /**
     * 
     *            1
     *        /       \
     *       3         5
     *      / \       / \
     *     8   2     9   7
     *    /
     *   10
     *   
     *   convert to:
     *   
     *   10 <-> 8 <-> 3 <-> 2 <-> 1 <-> 9 <-> 5 <-> 7
     *   
     * @param root
     * @return
     */
    private static BinaryTreeNode inOrderList(BinaryTreeNode root) {
        if (root == null) {
            return null;
        }

        // convert the left subtree to in-order list.
        BinaryTreeNode left = inOrderList(root.left);
        if (left != null) {
            // find the right-most node in the left in-order list.
            // this will become the new left side of the root.
            while (left.right != null) {
                left = left.right;
            }
            left.right = root;
            root.left = left;
        }
        
        // convert the right subtree to in-order list.
        BinaryTreeNode right = inOrderList(root.right);
        if (right != null) {
            // find the left-most node in the right in-order list.
            // this will become the new right side of the root.
            while (right.left != null) {
                right = right.left;
            }
            right.left = root;
            root.right = right;
        }
        
        return root;
    }

	public static void printAsList(BinaryTreeNode head) {
        Set<BinaryTreeNode> visited = new TreeSet<>();
        StringBuilder sb = new StringBuilder();
        printAsList(head, visited, sb);
        System.out.println(sb.toString());
    }

    private static void printAsList(BinaryTreeNode head, Set<BinaryTreeNode> visited, StringBuilder sb) {
        if (head == null) {
            return;
        }
        
        if (visited.contains(head)) {
            return;
        }
        
        sb.append(head.val);
        visited.add(head);
        if (head.right != null) {
            if (visited.contains(head.right)) {
                sb.append("<->").append(head.right.val);
            } else {
                sb.append("<->");
            }
        }
        printAsList(head.right, visited, sb);
    }

    private static BinaryTreeNode createTestBinaryTree() {
        BinaryTreeNode root = new BinaryTreeNode(1);
		BinaryTreeNode three = new BinaryTreeNode(3);
		BinaryTreeNode five = new BinaryTreeNode(5);
		BinaryTreeNode eight = new BinaryTreeNode(8);
		BinaryTreeNode two = new BinaryTreeNode(2);
		BinaryTreeNode nine = new BinaryTreeNode(9);
		BinaryTreeNode seven = new BinaryTreeNode(7);
		BinaryTreeNode ten = new BinaryTreeNode(10);
		
		root.left(three).right(five);
		three.left(eight).right(two);
		five.left(nine).right(seven);
		eight.left(ten);
        return root;
    }

    public static NAryTreeNode<Integer> createTestNAryTree() {
        NAryTreeNode<Integer> root = new NAryTreeNode<Integer>(1);
        NAryTreeNode<Integer> three = new NAryTreeNode<Integer>(3);
        NAryTreeNode<Integer> five = new NAryTreeNode<Integer>(5);
        NAryTreeNode<Integer> eight = new NAryTreeNode<Integer>(8);
        NAryTreeNode<Integer> two = new NAryTreeNode<Integer>(2);
        NAryTreeNode<Integer> nine = new NAryTreeNode<Integer>(9);
        NAryTreeNode<Integer> seven = new NAryTreeNode<Integer>(7);
        NAryTreeNode<Integer> ten = new NAryTreeNode<Integer>(10);
        
        root.addChild(three).addChild(five);
        three.addChild(eight).addChild(two);
        five.addChild(nine).addChild(seven);
        eight.addChild(ten);
        return root;
    }

    public static NAryTreeNode<String> createRandomNAryTree(int numLevels, int maxPerLevel, List<Integer> randVals, 
            PriorityQueue<NodeInfo> maxNodes, boolean cacheLineage) {
        List<NAryTreeNode<String>> roots = new ArrayList<>();
        int rootVal = rand.nextInt();
        NAryTreeNode<String> root = new NAryTreeNode<String>(Integer.toString(rootVal));
        root.toggleLineageCache(cacheLineage);
        randVals.add(rootVal);
        roots.add(root);
        NAryTreeNode<String> result = root;
        
        FixedSizeMaxPriorityQueue<NodeInfo> sortedNodes = new FixedSizeMaxPriorityQueue<>(1, Comparator.naturalOrder());
        for (int i = 0; i < numLevels; i++) {
            Iterator<NAryTreeNode<String>> itr = roots.iterator();
            while (itr.hasNext()) {
                root = itr.next();
                int numNodes = rand.nextInt(maxPerLevel);
                if (numNodes == 0) {
                    numNodes = 1;
                }
                for (int j = 0; j < numNodes; j++) {
                    int randVal = rand.nextInt();
                    randVals.add(randVal);
                    NAryTreeNode<String> child = new NAryTreeNode<String>(Integer.toString(randVal));
                    child.toggleLineageCache(cacheLineage);
                    root.addChild(child);
                    sortedNodes.add(new NodeInfo(child.getFQVString(), -1, child.getLineageNodes().size()));
                }
            }
            
            roots.clear();
            // choose the next nodes that will have children
            List<NAryTreeNode<String>> children = root.getChildren();
            int numChildren = children.size();
            int numWithChildren = rand.nextInt(numChildren);
            if (numWithChildren == 0) {
                numWithChildren = 1;
            }
            for (int k = 0; k < numWithChildren; k++) {
                int randChild = rand.nextInt(numChildren);
                NAryTreeNode<String> child = children.get(randChild);
                roots.add(child);
            }
        }
        
        NodeInfo maxNode = sortedNodes.peek();
        maxNodes.add(maxNode);
        return result;
    }

    public static void main(String[] args) throws Exception {
    		/*
    		 * 			  1
    		 *        /       \
    		 *       3         5
    		 *      / \       / \
    		 *     8   2     9   7
    		 *    /
    		 *   10
    		 */
    	    BinaryTreeNode root = createTestBinaryTree();
    		
    		System.out.println("Original tree:");
    		printTree(root);
    		
    		System.out.println("");
            System.out.println("Level order:");
    		List<List<Integer>> levelOrder = levelOrder(root);
    		for (List<Integer> level : levelOrder) {
                System.out.println(level);
            }
    		
    		System.out.println("");
    		System.out.println("Link peers:");
    		linkPeers(root);
    		printTree(root);
    
    		root = createTestBinaryTree();
            System.out.println("");
            System.out.println("Convert to ordered list (no cycle):");
    		root = convertToOrderedList(root, false);
            printAsList(root);
    
            root = createTestBinaryTree();
            System.out.println("");
            System.out.println("Convert to ordered list (with cycle):");
            root = convertToOrderedList(root, true);
            printAsList(root);
            
            System.out.println("");
            System.out.println("");
            /*
             *            1
             *        /       \
             *       3         5
             *      / \       / \
             *     8   2     9   7
             *    /
             *   10
             */
            NAryTreeNode<Integer> nroot = TreeStuff.createTestNAryTree();
            
            System.out.println("Original n-ary tree:");
            NAryTreeNode.printTree(nroot);
            
            NAryTreeNode<Integer> ten = NAryTreeNode.findNodeWithValue(nroot, 10);
            System.out.println("fqn(10) = " + ten.getFQVString());
            System.out.println("lineageNodes(10) = " + ten.getLineageNodes());
            
            NAryTreeNode<Integer> two = NAryTreeNode.findNodeWithValue(nroot, 2);
            System.out.println("fqn(2) = " + two.getFQVString());
            
            NAryTreeNode<Integer> nine = NAryTreeNode.findNodeWithValue(nroot, new Integer[]{1, 5, 9});
            System.out.println("fqn(1.5.9) = " + nine.getFQVString());
            
            nine = NAryTreeNode.findNodeWithValue(nroot, 9);
            System.out.println("fqn(9) = " + nine.getFQVString());
            
            nine = NAryTreeNode.findNodeWithValue(nroot, new Integer[]{1, 8, 9});
            System.out.println("fqn(1.8.9) = " + (nine != null ? nine.getFQVString() : "<null>"));
            
            NAryTreeNode<Integer> fail = NAryTreeNode.findNodeWithValue(nroot, new Integer[]{1, 5, 9, 6});
            System.out.println("fqn(1.5.9.6) = " + (fail != null ? fail.getFQVString() : "<null>"));
            
            Properties props = new Properties();
            props.setProperty("PSI.SSN", "blah1");
            props.setProperty("PSI.SSN.GermanSSN", "12345");
            props.setProperty("PSI.CCNum", "293842903470283402");
            props.setProperty("PSI.SSN.GermanSSN", "555572");
            props.setProperty("PSI.SSN.EnglishSSN", "0938084");
            props.setProperty("SystemT.Person", "blah");
            props.setProperty("SystemT.Person.Address", "blah");
            props.setProperty("SystemT.Person.PhoneNumber", "blah");
            props.setProperty("SystemT.Organization", "blah");
            props.setProperty("SystemT.Location", "blah");
            props.setProperty("SystemT.Date", "blah");
            props.setProperty("SystemT.Time", "blah");
            
            System.out.println("");
            System.out.println("Property names: " + props.keySet());
            SortedSet<NAryTreeNode<String>> roots = NAryTreeNode.extractHierarchies(props);
            for (NAryTreeNode<String> hierarchy : roots) {
                System.out.println("BUILT N-ARY TREE:");
                NAryTreeNode.printTree(hierarchy, true);
                System.out.println("====================");
            }

            System.out.println("");
            System.out.println("====================");
            System.out.println("TESTING RANDOM TREE PERFORMANCE WITH SMALL TREES");
            int numTests = 5;
            System.out.println("====================");
            System.out.println("LINEAGE CACHING ENABLED");
            testRandomTreePerf(numTests, true);
            System.gc();
    
            System.out.println("====================");
            System.out.println("LINEAGE CACHING DISABLED");
            testRandomTreePerf(numTests, false);
            System.gc();

            System.out.println("");
            System.out.println("====================");
            System.out.println("TESTING RANDOM TREE PERFORMANCE WITH LARGE TREES");
            numTests = 19;
            System.out.println("====================");
            System.out.println("LINEAGE CACHING ENABLED");
            testRandomTreePerf(numTests, true);
            System.gc();
    
            System.out.println("====================");
            System.out.println("LINEAGE CACHING DISABLED");
            testRandomTreePerf(numTests, false);
            System.gc();
        }

    private static void testRandomTreePerf(int numTests, boolean cacheLineage) throws Exception {
        FixedSizeMaxPriorityQueue<TreeInfo> creationTimes = new FixedSizeMaxPriorityQueue<>(1, Comparator.naturalOrder());
        FixedSizeMaxPriorityQueue<NodeInfo> maxNodes = new FixedSizeMaxPriorityQueue<>(1, Comparator.naturalOrder());
        List<NAryTreeNode<String>> roots = new ArrayList<>();
        
        for (int i = 1; i <= numTests; i++) {
            int numLevels = i*10;
            int maxPerLevel = numLevels/2;
//            System.out.println("Creating random tree with numLevels=" + numLevels + ", maxPerLevel=" + maxPerLevel);
            List<Integer> randVals = new ArrayList<>();
            long start = System.currentTimeMillis();
            NAryTreeNode<String> randTree = createRandomNAryTree(numLevels, maxPerLevel, randVals, maxNodes, cacheLineage);
            roots.add(randTree);
            long end = System.currentTimeMillis();
            long creationTime = end-start;
            int numNodes = randVals.size();
            creationTimes.add(new TreeInfo(creationTime, numNodes));
//            System.out.println("Time to create random tree: " + creationTime + " ms, numNodes=" + numNodes);
            
   //            NAryTreeNode.printTree(randTree, false);
//            int randIdx = rand.nextInt(numNodes);
//            Integer randVal = randVals.get(randIdx);
//            start = System.currentTimeMillis();
//            NAryTreeNode randNode = NAryTreeNode.findNodeWithValue(randTree, randVal.toString());
//            end = System.currentTimeMillis();
//            long searchTime = end-start;
//            int lineageSize = randNode.getLineageNodes().size();
//            System.out.println("random node: " + randNode.getValue() + ", lineage size = " + lineageSize + ", search time = " + searchTime + " ms");
        }

        System.out.println("______________________");
        System.out.println("STATS:");
        TreeInfo maxCreate = creationTimes.peek();
        System.out.println("Largest tree: numNodes=" + maxCreate.numNodes + ", creationTime=" + maxCreate.creationTime);
        
        NodeInfo maxNode = maxNodes.peek();
        String[] split = NAryTreeNode.getSplitFQV(maxNode.nodeValue);
        String root = split[0];
        String val = split[split.length-1];
        NAryTreeNode<String> maxNodeRoot = NAryTreeNode.findNodeWithValue(roots, root);
        long start = System.currentTimeMillis();
        NAryTreeNode<String> foundMax = NAryTreeNode.findNodeWithValue(maxNodeRoot, val);
        if (foundMax == null) {
            throw new Exception("Could not find max node " + val);
        }
        long end = System.currentTimeMillis();
        long valueSearchTime = end - start;
        System.out.println("Slowest search by value: nodeValue=" + val + ", lineageSize=" 
                            + maxNode.lineageSize + ", searchTime=" + valueSearchTime);
        
        start = System.currentTimeMillis();
        foundMax = NAryTreeNode.findNodeWithValue(maxNodeRoot, NAryTreeNode.getSplitFQV(maxNode.nodeValue));
        if (foundMax == null) {
            throw new Exception("Could not find max node " + val);
        }
        end = System.currentTimeMillis();
        long fqvSearchTime = end - start;
        System.out.println("Slowest search by FQV: nodeValue=" + val + ", lineageSize=" 
                            + maxNode.lineageSize + ", searchTime=" + fqvSearchTime);
        long totalMemMB = Runtime.getRuntime().totalMemory()/1000000;
        System.out.println("Heap size = " + totalMemMB + " MB");
        long freeMemMB = Runtime.getRuntime().freeMemory()/1000000;
        System.out.println("Free mem = " + freeMemMB + " MB");
        System.out.println("Used mem = " + (totalMemMB-freeMemMB) + " MB");
    }
    
    static class TreeInfo implements Comparable<TreeInfo> {
        
        long creationTime;
        int numNodes;
        
        public TreeInfo(long createTime, int numNodes) {
            this.creationTime = createTime;
            this.numNodes = numNodes;
        }

        @Override
        public int compareTo(TreeInfo o) {
            return Long.compare(this.numNodes, o.numNodes);
        }
        
    }
    
    static class NodeInfo implements Comparable<NodeInfo> {
        
        String nodeValue;
        long searchTime;
        int lineageSize;
        
        public NodeInfo(String nodeVal, long searchTime, int lineageSize) {
            this.nodeValue = nodeVal;
            this.searchTime = searchTime;
            this.lineageSize = lineageSize;
        }

        @Override
        public int compareTo(NodeInfo o) {
            return Integer.compare(this.lineageSize, o.lineageSize);
        }
        
    }

}
