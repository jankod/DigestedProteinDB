package hr.ja.test;

public class Ascii {

	public static void main(String[] args) {

		// assert x >= 0 && x < 256;
		for (int c = 0; c < 257; c++) {
			System.out.println(c + ": " + (char) c);
		}
	}
}
