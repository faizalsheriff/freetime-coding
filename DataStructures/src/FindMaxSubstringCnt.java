
public class FindMaxSubstringCnt {

	public static void main(String args[]) {

		String[] input = { "rat", "cat", "abc", "xyz", "abcxyz", "ratabccat",
				"xyzratcatabc" };

		String maxSubstring = getMaxSubstring(input);
		System.out.println(maxSubstring);

	}

	public static String getMaxSubstring(String[] input) {
		Object[][] twoDStorage = null;

		twoDStorage = transform(input);

		if (twoDStorage == null)
			return "NOTHING";
		int row = 1;
		int col = 0;
		String curVal = "";

		for (; row <= input.length ; row++) {
			curVal = (String) twoDStorage[row][col];

			for (col=1; col <= input.length; col++) {

				if (((String) twoDStorage[0][col]).contains(curVal)) {

					if (row >= 2) {
						twoDStorage[row][col] = (Integer) twoDStorage[row - 1][col]
								+ (Integer) twoDStorage[row][col];
					}

					twoDStorage[row][col] = (Integer) twoDStorage[row][col] + 1;

				}

			}
		}

		// scan last row
		int max = 0;
		String maxstr = "";
		for (row = input.length; row < input.length + 1; row++) {

			for (col = 1; col < input.length + 1; col++) {

				if ((Integer) twoDStorage[row][col] > max) {
					max = (Integer) twoDStorage[row][col];
					maxstr = (String) twoDStorage[0][col];

				}
			}

		}
		System.out.println(max);

		return maxstr;
	}

	public static Object[][] transform(String[] input) {

		if (input == null) {
			return null;
		}
		Object[][] twoDStorage = new Object[input.length + 1][input.length + 1];

		int row = 0;
		int col = 1;

		for (; col < input.length + 1; col++) {
			twoDStorage[row][col] = input[col - 1];
		}

		row = 1;
		col = 0;
		for (; row < input.length + 1; row++) {

			twoDStorage[row][col] = input[row - 1];

		}

		return twoDStorage;
	}

}