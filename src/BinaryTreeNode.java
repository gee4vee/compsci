
public class BinaryTreeNode implements Comparable<BinaryTreeNode>{
    int val;
    BinaryTreeNode left;
    BinaryTreeNode right;
    /**
     * The node that is on the same level immediately to the right of this node.
     */
    BinaryTreeNode peer;
    
    BinaryTreeNode(int x) { val = x; }
    
    public BinaryTreeNode left(BinaryTreeNode n) {
        this.left = n;
        return this;
    }
    
    public BinaryTreeNode right(BinaryTreeNode n) {
        this.right = n;
        return this;
    }
    
    @Override
    public String toString() {
        return "TreeNode["+this.val+"]";
    }

    @Override
    public int compareTo(BinaryTreeNode o) {
        return Integer.compare(this.val, o.val);
    }
    
    
}
