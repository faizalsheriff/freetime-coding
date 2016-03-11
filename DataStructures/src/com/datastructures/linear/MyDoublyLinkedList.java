package com.datastructures.linear;

import com.datastructures.linear.doublylinkedlist.Node;

public class MyDoublyLinkedList {

	private int value;
	private Node first;
	private Node last;
	private int size;

	public void add(int newVal) {
		Node ne = new Node(newVal);
		if (first == null) {
			first = ne;
			last = first;
			size++;
		} else {
			last.setNext(ne);
			Node prev = last;
			last = last.getNext();
			last.setPrevious(prev);
			size++;
		}
	}

	public int remove(int val) {

		if (first == null)
			return -1;
		else {

			for (Node ind = first; ind.getNext() != null; ind = ind.getNext()) {

				if (ind.getNodeValue() == val) {

					if (ind.getPrevious() == null && ind.getNext() == null)// only
																			// one
																			// node
					{
						size--;
						first = null;
						last = first;
						return val;
					} else if (ind.getPrevious() == null
							&& ind.getNext() != null) {// if node is first
						size--;
						first = ind.getNext();
						ind=null;
						return val;
					} else if (ind.getPrevious() != null
							&& ind.getNext() == null) {// if node is last
						size--;
						last = ind.getPrevious();
						ind= null;
						return val;
					} else { //middle node

						ind.getPrevious().setNext(ind.getNext());// set forward
																	// pointer
						ind.getNext().setPrevious(ind.getPrevious());// set back
																		// ward
																		// pointer
						ind = null;
						size--;
						return val;
					}
				}
			}
			return -1;
		}
	}

	public int peek(int val) {
		if (first == null)
			return -1;

		else {
			for (Node ind = first; ind.getNext() != null; ind = ind.getNext()) {
				if (ind.getNodeValue() == val) {
					return val;
				}
			}
		}
		return -1;
	}

	public int size() {
		return size;
	}

}
