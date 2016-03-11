import rita.wordnet.RiWordnet;;

import java.util.*;
import java.io.*;
public class projectClassify {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Map<String, String> listOfInterests = new HashMap();
		
		listOfInterests.put("antiques","antiques");
		listOfInterests.put("art-collecting","art-collecting");
		listOfInterests.put("arts","arts");
		listOfInterests.put("crafts","crafts");
		listOfInterests.put("astrology","astrology");
		listOfInterests.put("astronomy","astronomy");
		listOfInterests.put("beadwork","beadwork");
		listOfInterests.put("ballet-dancing","ballet-dancing");
		listOfInterests.put("ballroom-dancing","ballroom-dancing");
		listOfInterests.put("belly-dancing","belly-dancing");
		listOfInterests.put("birdwatching","birdwatching");
		listOfInterests.put("break-dancing","break-dancing");
		listOfInterests.put("tattoos","tattoos");
		listOfInterests.put("body-piercing","body-piercing");
		listOfInterests.put("cake-decorating","cake-decorating");
		listOfInterests.put("camping","camping");
		listOfInterests.put("candle-making","candle-making");
		listOfInterests.put("coin-collecting","coin-collecting");
		listOfInterests.put("coloring-pages","coloring-pages");
		listOfInterests.put("computer-games","computer-games");
		listOfInterests.put("video-games","video-games");
		listOfInterests.put("cooking","cooking");
		listOfInterests.put("crochet","crochet");
		listOfInterests.put("cruising","cruising");
		listOfInterests.put("dancing","dancing");
		listOfInterests.put("dog-training","dog-training");
		listOfInterests.put("diving","diving");
		listOfInterests.put("drawing","drawing");
		listOfInterests.put("embroidery","embroidery");
		listOfInterests.put("family-vacations","family-vacations");
		listOfInterests.put("fishing","fishing");
		listOfInterests.put("flamenco","flamenco");
		listOfInterests.put("floral-arrangements","floral-arrangements");
		listOfInterests.put("gardening","gardening");
		listOfInterests.put("genealogy","genealogy");
		listOfInterests.put("graffiti","graffiti");
		listOfInterests.put("art","art");
		listOfInterests.put("graphology","graphology");
		listOfInterests.put("handmade-jewelry","handmade-jewelry");
		listOfInterests.put("hiking","hiking");
		listOfInterests.put("dancing","dancing");
		listOfInterests.put("horse-riding","horse-riding");
		listOfInterests.put("hunting","hunting");
		listOfInterests.put("ice-skating","ice-skating");
		listOfInterests.put("jewelry-making","jewelry-making");
		listOfInterests.put("judo","judo");
		listOfInterests.put("kayaking","kayaking");
		listOfInterests.put("kickboxing","kickboxing");
		listOfInterests.put("knitting","knitting");
		listOfInterests.put("kung-fu","kung-fu");
		listOfInterests.put("martial-arts","martial-arts");
		listOfInterests.put("magic","magic");
		listOfInterests.put("music","music");
		listOfInterests.put("blogging","blogging");
		listOfInterests.put("chatting","chatting");
		listOfInterests.put("dating","dating");
		listOfInterests.put("games","games");
		listOfInterests.put("music","music");
		listOfInterests.put("social-networking","social-networking");
		listOfInterests.put("palmistry","palmistry");
		listOfInterests.put("paper-crafts","paper-crafts");
		listOfInterests.put("paragliding","paragliding");
		listOfInterests.put("pet-adoption","pet-adoption");
		listOfInterests.put("photography","photography");
		listOfInterests.put("physical-exercises","physical-exercises");
		listOfInterests.put("quilting","quilting");
		listOfInterests.put("radio","radio");
		listOfInterests.put("reading","reading");
		listOfInterests.put("riddles","riddles");
		listOfInterests.put("sculpting","sculpting");
		listOfInterests.put("sewing","sewing");
		listOfInterests.put("singing","singing");
		listOfInterests.put("space-exploration","space-exploration");
		listOfInterests.put("stamp-collecting","stamp-collecting");
		listOfInterests.put("sudoku-puzzles","sudoku-puzzles");
		listOfInterests.put("mountain-climbing","mountain-climbing");
		listOfInterests.put("sailing","sailing");
		listOfInterests.put("boating","boating");
		listOfInterests.put("dancing","dancing");
		listOfInterests.put("scrapbooking","scrapbooking");
		listOfInterests.put("diving","diving");
		listOfInterests.put("skateboarding","skateboarding");
		listOfInterests.put("skydiving","skydiving");
		listOfInterests.put("snow-skiing","snow-skiing");
		listOfInterests.put("snowboarding","snowboarding");
		listOfInterests.put("speed-skating","speed-skating");
		listOfInterests.put("surfing","surfing");
		listOfInterests.put("swimming","swimming");
		listOfInterests.put("treasure-hunting","treasure-hunting");
		listOfInterests.put("trekking","trekking");
		listOfInterests.put("traveling","traveling");
		listOfInterests.put("wood-carving","wood-carving");
		listOfInterests.put("woodworking","woodworking");
		listOfInterests.put("writing","writing");
		
		
		/*
		 * Wordnet Ontology based Classification
		 */
		
		String userInterest = "playing cricket";
		
		/*
		 * Pre-Processing the user interests containing " " with "-" to replace for giving inside WordNet
		 */
		if(userInterest.contains(" "))
			userInterest = userInterest.replace(" ", "-");
		
		RiWordnet wordNet = new RiWordnet();
		wordNet.ignoreCompoundWords(false);
		Set setItr = listOfInterests.entrySet();
		Iterator indexValue = setItr.iterator();
		/*
		* Here we are calculating similarity with interest to all other interets
		*/
		
		while(indexValue.hasNext()) {
			Map.Entry me = (Map.Entry)indexValue.next();
			String interestFromHashMap = (String)me.getKey();
			String pos = wordNet.getBestPos(interestFromHashMap);
			if(interestFromHashMap.contains("-"))
				pos="v";
			float comparisonScore = 100;
			if(pos != null)
				comparisonScore = wordNet.getDistance(interestFromHashMap, userInterest, pos);
			if((int)Math.ceil(comparisonScore*1000000) < 150000) 
				System.out.println(interestFromHashMap +" and "+ userInterest + " are similar");
			/*
			* Abovw, we are keeping threshold of 0.15*1000000..any value less than this is similar
			* with in this if, link to Db that will add this UID to that table
			* Keep a flag, if any interest doesnt match the user interest create a table and add uid to that table as first entry
			*/
		}
		
		/*
		 * From here we call upon the Data base connectivity to store the list of interests on which we want to add the user ID to DB 
		 */
		
		
		
	}

}
