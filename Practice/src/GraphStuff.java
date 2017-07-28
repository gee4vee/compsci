import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * @author gvalenc
 *
 */
public class GraphStuff {
    
    /**
     * A bipartite graph can be sliced with one slice such that the nodes on each side are disconnected. In other words, there exists 
     * two sets of nodes such that a node in each set is not connected with another node in that same set. Nodes across sets can be 
     * connected. Assume there are no disjoint graphs.
     * 
     * @param n One of the nodes in a connected graph.
     * 
     * @return
     */
    public static <T extends Comparable<T>> boolean isBipartite(NAryTreeNode<T> n) {
        /*
         * keep track of visited nodes using a set. also use a List to track the disjoint sets.
         * using BFS, for each node's child, if it has not been visited, check if it is not connected to any 
         * node in each of the disjoint sets. if it is not, add it. otherwise, continue to next node.
         * 
         * continue until all nodes have been visited. if at the end the size of all the disjoint sets equals the number of nodes
         * in the graph, the graph is bipartite. otherwise, it is not.
         */
        Set<NAryTreeNode<T>> visited = new HashSet<>();
        // for a single slice we expect two disjoint sets.
        Set<NAryTreeNode<T>> disjointSet1 = new HashSet<>();
        Set<NAryTreeNode<T>> disjointSet2 = new HashSet<>();
        List<Set<NAryTreeNode<T>>> disjointSets = new ArrayList<>();
        disjointSets.add(disjointSet1);
        disjointSets.add(disjointSet2);
        Queue<NAryTreeNode<T>> q = new LinkedList<>();
        
        q.offer(n);
        visited.add(n);
        while (!q.isEmpty()) {
            int count = q.size();
            for (int i = 0; i < count; i++) {
                NAryTreeNode<T> node = q.poll();
                if (node != null) {
                    for (NAryTreeNode<T> child : node.getChildren()) {
                        if (visited.add(child)) { // find a disjoint set for the child if we haven't visited it before.
                            findDisjointSetForNode(disjointSets, node);
                            q.offer(child);
                            findDisjointSetForNode(disjointSets, child);
                        }
                    }
                }
            }
        }
        
        // all the nodes must have been placed into a disjoint set.
        int total = 0;
        for (Set<NAryTreeNode<T>> set : disjointSets) {
            total += set.size();
        }
        
        return total == visited.size();
    }

    private static <T extends Comparable<T>> void findDisjointSetForNode(List<Set<NAryTreeNode<T>>> disjointSets, NAryTreeNode<T> node) {
        Set<NAryTreeNode<T>> dSet = null;
        if (disjointSets.isEmpty()) {
            dSet = new HashSet<>();
            disjointSets.add(dSet);
        } else {
            for (Set<NAryTreeNode<T>> set : disjointSets) {
                dSet = set;
                for (NAryTreeNode<T> sNode : set) {
                    // see if the child is connected to one of the nodes in this disjoint set.
                    if (sNode.hasChild(node)) {
                        dSet = null;
                        break;
                    }
                }
                if (dSet != null) {
                    break;
                }
            }
        }
        if (dSet != null) {
            dSet.add(node);
        }
    }
    
    public static void main(String[] args) {
        /*
         * NOT bipartite:
         * 
         *          1
         *         / \
         *        2---3
         */
        NAryTreeNode<Integer> one = new NAryTreeNode<>(1);
        NAryTreeNode<Integer> two = new NAryTreeNode<>(2);
        NAryTreeNode<Integer> three = new NAryTreeNode<>(3);
        one.addChild(two).addChild(three);
        two.addChild(one).addChild(three);
        three.addChild(one).addChild(two);
        
        System.out.println("isBipartite(triangleTree)=" + isBipartite(one));
        /*
         * IS bipartite:
         * 
         *          1
         *         / \
         *        2   3
         */
        two.removeAllChildren().addChild(one);
        three.removeAllChildren().addChild(one);
        System.out.println("isBipartite(1-2, 1-3)=" + isBipartite(one));
        
        /*
         * https://stackoverflow.com/questions/3399340/how-do-i-implement-a-bipartite-graph-in-java
         */
        one.removeAllChildren();
        two.removeAllChildren();
        three.removeAllChildren();
        NAryTreeNode<Integer> four = new NAryTreeNode<>(4);
        NAryTreeNode<Integer> five = new NAryTreeNode<>(5);
        NAryTreeNode<Integer> six = new NAryTreeNode<>(6);
        NAryTreeNode<Integer> seven = new NAryTreeNode<>(7);
        one.addChild(two).addChild(three);
        two.addChild(four).addChild(five).addChild(six);
        three.addChild(one).addChild(four).addChild(five).addChild(six);
        four.addChild(two).addChild(three).addChild(seven);
        five.addChild(two).addChild(three).addChild(seven);
        six.addChild(two).addChild(three).addChild(seven);
        System.out.println("isBipartite(sampleFromWeb)=" + isBipartite(one));
        // add a connection between two and three to make it non-bipartite.
        two.addChild(three);
        three.addChild(two);
        System.out.println("isBipartite(sampleFromWeb2)=" + isBipartite(one));
    }

}
