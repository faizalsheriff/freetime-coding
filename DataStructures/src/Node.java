import java.util.List;


public class Node {

	private String nodeState;
	private int nodeValue;
	private Node leftNode;
	private Node rightNode;
	private Node parentNode;
	private boolean nodeBalanced;
	
	public Node(Integer item) {
		this.nodeValue = item;
	}

	public boolean isNodeBalanced() {
		return nodeBalanced;
	}

	public void setNodeBalanced(boolean nodeBalanced) {
		this.nodeBalanced = nodeBalanced;
	}

	public List<Node> getNodeState() {
		return nodeState;
	}
	
	public void setNodeState(String nodeState) {
		this.nodeState = nodeState;
	}
	
	public int getNodeValue() {
		return nodeValue;
	}
	
	public void setNodeValue(int nodeValue) {
		this.nodeValue = nodeValue;
	}
	
	public Node getLeftNode() {
		return leftNode;
	}
	
	public void setLeftNode(Node leftNode) {
		this.leftNode = leftNode;
	}
	
	public Node getRightNode() {
		return rightNode;
	}
	
	public void setRightNode(Node rightNode) {
		this.rightNode = rightNode;
	}
	
	public Node getParentNode() {
		return parentNode;
	}
	
	public void setParentNode(Node parentNode) {
		this.parentNode = parentNode;
	}
	
	public boolean hasParentNode() {
		if(this.parentNode ==null)
			return false;
		else 
			return true;
	}

	public Node createLeftChildNode(Node parentNode, int nodeValue2) {
		Node node = new Node(nodeValue2);
		node.setParentNode(parentNode);
		parentNode.setLeftNode(node);
		return parentNode;
		
	}
	
	public Node createRightChildNode(Node parentNode, int nodeValue2) {
		Node node = new Node(nodeValue2);
		node.setParentNode(parentNode);
		parentNode.setRightNode(node);
		return parentNode;
		
	}
	
	
	public List<Node> getAdjacentNode()
	{
		
		return null;
	
	}
	
	
	
}
