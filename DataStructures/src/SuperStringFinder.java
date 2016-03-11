public class SuperStringFinder {

	public static void main(String[] args) {
		System.out.println("ratcatabcxyzratabccatabcxyz".contains("rat"));

		String[] input = { "rat", "cat", "ratcatabcxyzratabccatabcxyz", "abc",
				"xyz", "abcxyz", "ratabccat", "xyzratcatabc" };

		System.out.println(getMaxString(input));
		
		int[] bl ={1,2,3,4,5,6};
		String[] s = (String)bl;
		System.out.println(String.value);

	}

	public static String getMaxString(String[] input) {

		int[] countMap = new int[input.length];
		for (int outInd = 0; outInd < input.length; outInd++) {
			for (int inInd = 0; inInd < input.length; inInd++) {
				if (input[inInd].contains(input[outInd])) {

					countMap[inInd] = countMap[inInd] + 1;

				}

			}

		}
		
		//countMap.to
		
		System.out.println(Math.pow(-2, -25));

		// search max value
		int outInd = 0;
		int max = 0;
		int sipStringLoc = -1;
		for (; outInd < input.length; outInd++) {

			if (countMap[outInd] > max) {
				max = countMap[outInd];
				sipStringLoc = outInd;
			}

		}

		if (sipStringLoc > -1) {
			return input[sipStringLoc];
		} else {
			return "Nothing";
		}

		
		
		
	}

}