package com.datastructures.linear.linkedlist;

public class LinkedListMergeSort {

	

		private Node sL;

		public LinkedListMergeSort(Node root){
		this.sL = root;
		}


		public void sort() throws Exception{

		if(sL!=null){
		Node last=sL;
		for(;last.getNext()!=null;last=last.getNext());

		mergeSort(sL, last);

		}
		}

		public void mergeSort(Node f, Node l) throws Exception{


		if(f!=l){

		Node mid = getMidNod(f, l);
/*
		mergeSort(f, mid);
		mergeSort(mid.getNext(), l);
		merge(f,mid,l);*/
		mergeLists(f,mid);
		
		}

		}

		public Node getMidNod(Node f, Node l) throws Exception{

		if(f==null){
		throw new Exception();
		}else if(l==null){
		return f;
		}else{
		int cnt=1;

		for (Node n=f; n.getNext()!=l;n=n.getNext()){
		cnt++;
		}

		cnt=cnt/2;
		Node mid=f;

		for(;cnt>0;cnt--){
		mid= mid.getNext();

		}

		return mid;

		}
		}

		public void merge(Node l, Node m, Node r){
			
		if(l==m || m==r){
				if(l.getValue()>r.getValue()){
				int t= l.getValue();
				l.setValue(r.getValue());
				r.setValue(t);
				}
				return;
		}

		int oC= getNodesCnt(l,m);
		int iC= getNodesCnt(m,r)+1;
			
		Node pL= l;
		while(oC>=0)	{
			
			Node rL=m;
			
			while(iC>=0){
				
				if(pL.getValue()>rL.getValue()){
						
					//create new node with r value
					Node n = new Node(rL.getValue());
				
					n.setNext(l.getNext());
					l.setNext(n);

					//
					int t= l.getValue();
					l.setValue(l.getNext().getValue());
					l.getNext().setValue(t);

					
					
					if(rL.getNext()!=null){
						rL.setValue(rL.getNext().getValue());
					}
					rL.setNext(rL.getNext().getNext());
					
					iC--;
				}
				rL=rL.getNext();
				iC--;
				
			}
			
			pL=pL.getNext();
			oC--;
		}
		
		
	/*	for(Node pL= l; pL.getNext()!=m; pL= pL.getNext()){
			for(Node rL=m; rL.getNext()!=r||rL!=null||rL.getNext()!=null;rL=rL.getNext()){
			if(pL.getValue()>rL.getValue()){
				
				//create new node with r value
				Node n = new Node(rL.getValue());
				
				//since we don't have left'e previous insert after left and swap
				n.setNext(l.getNext());
				l.setNext(n);

				int t= l.getValue();
				l.setValue(l.getNext().getValue());
				l.getNext().setValue(t);

				rL.setValue(rL.getNext().getValue());
				rL.setNext(rL.getNext().getNext());
				
				if(rL.getNext()==null){
					break;
				}
			

				
		}
		}
		}*/
		
		
		
		}


		private int getNodesCnt(Node l, Node m) {
			// TODO Auto-generated method stub
			int cnt=0;
			for(Node n =l; n.getNext()!=m; n= n.getNext(),cnt++);
			return cnt;
		}

		
		
		Node mergeLists(Node list1, Node list2) {
			  if (list1 == null) return list2;
		      if (list2 == null) return list1;

			  if (list1.getValue() < list2.getValue()) {
			    list1.setNext(mergeLists(list1.getNext(), list2));
			    return list1;
			  } else {
			    list2.setNext(mergeLists(list1,list2.getNext()));
			    return list2;
			  }
			}
		
		

		public void display() {
			for(Node ptr=sL; ptr.getNext()!=null; ptr=ptr.getNext()){
				System.out.println(ptr.getValue());
			}

		}

		}
