
public class Node {
	
	String value;
	Node left;
	Node right;
	
	public Node(String value) {
		this.value = value;
	}
	
	public Node(String value, Node left, Node right) {
		this(value);
		this.left = left;
		this.right = right;
	}
	
	public Node left(Node n) {
	    this.left = n;
	    return this;
	}
	
	public Node right(Node n) {
	    this.right = n;
	    return this;
	}
	
	@Override
	public String toString() {
	    return this.value;
	}
}
