package com.datastructures.linear.arrays;

public class MatrixRotator {
	private int sc = 0, sr = 0, r = 0, c = 0, or = 0, oc = 0, mr = 3, mc = 3, m = 3;// 4x4
	private int totC = 0;
	
	private char[][] in;
	
	MatrixRotator(char[][] d2){
		this.in = d2;
	}

	

	char[][] rotate90C() {
		if (isSqr(in)) {

			while (oc < mc && or < mr) {
				// to copy inside the array
				r = sr;
				c = sc;

				while (sc < mc) {

					// complete rotate
					totC=4*m;
					char cur = in[sr][sc];

					// single rotate
					if (sc == sr) {
						incRow();

						incCol();
						decRow();
						decCol();

						in[r][c] = cur;
					} else {
						decCol();
						incRow();
						incCol();
						decRow();
						decCol();
						in[r][c] = cur;
					}
					// decrement row and column to traverse inside template
					sc++;
					
				}
				
				oc++;
				or++;
				mr--;
				mc--;
				sc = oc;
				sr = or;
				m = m - 2; // dec
			}

		}
		return in;
	}
	
	
	public void display(){
		for(int i=0;i<4;i++){
			for(int j=0;j<4;j++){
				System.out.print(in[i][j]);
			}
			System.out.println();
		}
	}

	static boolean isSqr(char[][] sq) {
		int prev = -1;
		if (sq.length > 0) {
			if (prev == -1)
				prev = sq[0].length;
			int i = 0;
			for (; (i < sq.length && prev == sq[i].length); i++)
				;
			if (i == sq.length) {
				return true;
			}
		}
		return false;
	}

	private void incRow() {

		while (r < mr && totC > 1) {

			totC--;

			in[r][c] = in[++r][c];
			continue;

		}
	}

	private void incCol() {

		while (c < mc && totC > 1) {
			totC--;
			in[r][c] = in[r][++c];
			continue;
		}

	}

	private void decRow() {

		while (r > 0 && totC > 1) {
			totC--;
			in[r][c] = in[--r][c];
			continue;
		}
	}

	private void decCol() {

		while (c > 0 && totC > 1) {
			totC--;
			in[r][c] = in[r][--c];
			continue;
		}

	}

}
