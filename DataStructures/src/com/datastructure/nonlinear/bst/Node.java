package com.datastructure.nonlinear.bst;

public class Node {

	private int value;
	private Node left;
	private Node right;
	private Node parent;
	private boolean isBalanced;
	private int height;
	
	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public boolean isBalanced() {
		return isBalanced;
	}

	public void setBalanced(boolean isBalanced) {
		this.isBalanced = isBalanced;
	}

	public Node getLeft() {
		return left;
	}

	public void setLeft(Node left) {
		this.left = left;
	}

	public Node getRight() {
		return right;
	}

	public void setRight(Node right) {
		this.right = right;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public Node(int ne) {
		this.value=ne;
	}
	
	

}
