package mywordplay;

import rita.wordnet.RiWordnet;

public class SimpleWordPlay {

	public static void main(String[] args) {
		
		RiWordnet wordNet = new RiWordnet();
		System.out.println(wordNet.getBestPos("my picture"));
		System.out.println(wordNet.getDistance("music","music-listen",wordNet.getBestPos("music-listen")));
		

	}

}
