


public class BinarySearchTree {
	
	
	private Node bst;
	public Node getBst() {
		return bst;
	}




	public void setBst(Node bst) {
		this.bst = bst;
	}



	private int[] unsortedList ;
	
	BinarySearchTree(int[]  usortedList){
		
		this.unsortedList=usortedList;
		
	}
	
	
	
	
	public void constructBST(){
		
		for (int item:unsortedList){
			
			//create node
			Node node= new Node(item);
			
			insertNode(node);
			
		}
	}


	public void inOrder(Node Root)
    {
        if(Root != null)
        {
            inOrder(Root.getLeftNode());
            System.out.print(Root.getNodeValue() + " ");
            inOrder(Root.getRightNode());
        }
    }
	
	
	public void postOrder(Node Root)
    {
        if(Root != null)
        {
        	
        	System.out.print(Root.getNodeValue() + " ");
        	
            inOrder(Root.getLeftNode());
            
            inOrder(Root.getRightNode());
        }
    }


	private void insertNode(Node currentNode) {
		
		if(bst==null){//current node is root
		bst=currentNode;	
		//return;
		}else{
			findNextAvaliablePos(bst, currentNode);
		}
	}
	
	public void preOrder(Node Root) {
		if(Root != null)
        {
        	
        	System.out.print(Root.getNodeValue() + " ");
        	
            inOrder(Root.getLeftNode());
            
            inOrder(Root.getRightNode());
        }
		
	}



	private void findNextAvaliablePos(Node bst2, Node currentNode) {
		if (currentNode.getNodeValue()<bst2.getNodeValue()){
			handleLeftSubtree(bst2, currentNode);
		}
		
		else if (currentNode.getNodeValue()>=bst2.getNodeValue()){
			handleRightSubtree(bst2, currentNode);
		}
	}




	private void handleRightSubtree(Node rightBST, Node currentNode) {
		
		/*
		if(currentNode.getNodeValue()<rightBST.getNodeValue() &&
				rightBST.getRightNode()!=null && rightBST.isNodeBalanced())
		{
			handleLeftSubtree(rightBST.getLeftNode(), currentNode);
		}
		
		else */if(/*currentNode.getNodeValue() >= rightBST.getNodeValue()
				&&*/ rightBST.getRightNode()!=null &&rightBST.isNodeBalanced())
		{
			findNextAvaliablePos(rightBST.getRightNode(), currentNode);
		}
		
		else if(/*currentNode.getNodeValue()>=rightBST.getNodeValue() && */rightBST.getRightNode()==null)
		{
			rightBST.setRightNode(currentNode);
			if(rightBST.getLeftNode()!=null){
				rightBST.setNodeBalanced(true);
			}
			setParent(rightBST, currentNode);
		}
		
		
		else if(/*currentNode.getNodeValue() >= rightBST.getNodeValue() && */rightBST.getRightNode()!=null
				&& !rightBST.isNodeBalanced()){
			
			rotateLeft(rightBST, currentNode);
			rightBST.setNodeBalanced(true);
			setParent(rightBST, currentNode);
			
		}
		
		
		else if(/*currentNode.getNodeValue() < rightBST.getNodeValue() && */rightBST.getLeftNode()!=null
				&& !rightBST.isNodeBalanced()){
			
			rotateRight(rightBST, currentNode);
			rightBST.setNodeBalanced(true);
			setParent(rightBST, currentNode);
		}
		
		
	}




	private void setParent(Node rightBST, Node currentNode) {
		currentNode.setParentNode(rightBST);
		
		while(currentNode.hasParentNode()){
			currentNode= currentNode.getParentNode();
		}
		bst=currentNode;
	}



	private void handleLeftSubtree(Node leftBST, Node currentNode) {
		
		if(/*currentNode.getNodeValue()<leftBST.getNodeValue() &&*/
				leftBST.getLeftNode()!=null && leftBST.isNodeBalanced())
		{
			findNextAvaliablePos(leftBST.getLeftNode(), currentNode);
		}
		
		/*else if(currentNode.getNodeValue() >= leftBST.getNodeValue()
				&& leftBST.getRightNode()!=null &&leftBST.isNodeBalanced())
		{
			handleRightSubtree(leftBST.getRightNode(), currentNode);
		}*/
		
		else if(/*currentNode.getNodeValue()<leftBST.getNodeValue() && */leftBST.getLeftNode()==null)
		{
			leftBST.setLeftNode(currentNode);
			setParent(leftBST, currentNode);
			if(leftBST.getRightNode()!=null){
				leftBST.setNodeBalanced(true);
			}
		}
		
		else if(/*currentNode.getNodeValue() < leftBST.getNodeValue() && */leftBST.getLeftNode()!=null
				&& !leftBST.isNodeBalanced()){
			
			rotateRight(leftBST, currentNode);
			leftBST.setNodeBalanced(true);
			setParent(leftBST, currentNode);
		}
		
		
		else if(currentNode.getNodeValue() >= leftBST.getNodeValue() && leftBST.getRightNode()!=null
				&& !leftBST.isNodeBalanced()){
			
			rotateLeft(leftBST, currentNode);
			leftBST.setNodeBalanced(true);
			setParent(leftBST, currentNode);
		}
		
		
	}



	private void rotateRight(Node leftBST, Node currentNode) {
		/*Node node = new Node(leftBST.getNodeValue());
		leftBST.setRightNode(node);*/
		leftBST=leftBST.createRightChildNode(leftBST, leftBST.getNodeValue());
		if(currentNode.getNodeValue()< leftBST.getLeftNode().getNodeValue()){
		
		leftBST.setNodeValue(leftBST.getLeftNode().getNodeValue());
	    //leftBST.setLeftNode(currentNode);
		leftBST=leftBST.createLeftChildNode(leftBST, currentNode.getNodeValue());
		}else if(currentNode.getNodeValue()>= leftBST.getLeftNode().getNodeValue()){
		
			leftBST.setNodeValue(currentNode.getNodeValue());
			
					
		}
		
		//currentNode.setParentNode(leftBST);
		
	}



	private void rotateLeft(Node leftBST, Node currentNode) {

		/*Node node = new Node(leftBST.getNodeValue());
		leftBST.setLeftNode(node);*/
		leftBST=leftBST.createLeftChildNode(leftBST,leftBST.getNodeValue());
		if(currentNode.getNodeValue()>= leftBST.getRightNode().getNodeValue()){
		leftBST.setNodeValue(leftBST.getRightNode().getNodeValue());
		
		//leftBST.setRightNode(currentNode);
		leftBST.createRightChildNode(leftBST, currentNode.getNodeValue());
		}else if(currentNode.getNodeValue()< leftBST.getRightNode().getNodeValue()){
			leftBST.setNodeValue(currentNode.getNodeValue());
		}
		//currentNode.setParentNode(leftBST);
	}




	



	
	
	

}
