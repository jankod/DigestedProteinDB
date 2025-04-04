package hr.pbf.digestdb.demo;

import java.math.BigInteger;

public class AccessionBase36BigInteger {
	private static final String BASE36_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static long decodeBase36custom(String accession) throws ArithmeticException {
		long result = 0;
		for(char c : accession.toCharArray()) {
			// Pomnoži trenutni rezultat s 36
			if(result > Long.MAX_VALUE / 36) {
				throw new ArithmeticException("Accession broj prelazi long: " + accession);
			}
			result *= 36;

			// Dodaj vrijednost trenutnog znaka
			int digit;
			if(c >= '0' && c <= '9') {
				digit = c - '0';
			} else if(c >= 'A' && c <= 'Z') {
				digit = c - 'A' + 10;
			} else {
				throw new IllegalArgumentException("Neispravan znak u accession broju: " + c);
			}

			if(Long.MAX_VALUE - result < digit) {
				throw new ArithmeticException("Accession broj prelazi long: " + accession);
			}
			result += digit;
		}
		return result;
	}

	public static String encodeBase36custom(long value) {
		if(value < 0) {
			throw new IllegalArgumentException("Vrijednost mora biti pozitivna");
		}
		StringBuilder sb = new StringBuilder();
		do {
			sb.insert(0, BASE36_CHARS.charAt((int) (value % 36)));
			value /= 36;
		}
		while(value > 0);
		return sb.toString();
	}

	public static long decodeBase36(String accession) throws ArithmeticException {
		BigInteger value = new BigInteger(accession, 36);
		if(value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
			throw new ArithmeticException("Accession broj prelazi long: " + accession);
		}
		return value.longValue();
	}

	public static String encodeBase36(long value) {
		return BigInteger.valueOf(value).toString(36).toUpperCase();
	}

	public static void main(String[] args) {
//		String accession = "Z9Z9Z9Z9Z9";
//		BigInteger value = new BigInteger(accession, 36);
//		System.out.println("Točna vrijednost: " + value);
//		System.out.println("Obrezano u long: " + value.longValue());
//		System.out.println("Long.MAX_VALUE: " + Long.MAX_VALUE);
//		System.out.println("Stane u long? " + (value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0));
//
//		System.out.println("Custom Base36: " + toBase36(accession));
//		System.out.println("Custom Base36: " + fromBase36(toBase36(accession)));

		String accession = "Z9Z9Z9Z9Z9";
        System.out.println("Uneseni string: " + accession);
        BigInteger value = new BigInteger(accession, 36);
        System.out.println("Točna vrijednost: " + value);
        System.out.println("Obrezano u long: " + value.longValue());
        System.out.println("Long.MAX_VALUE: " + Long.MAX_VALUE);
        System.out.println("Stane u long? " + (value.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0));
	}



	public static void main4(String[] args) {
		String[] tests = { "P12345", "Q9Z9Z9", "A0A123B4C5", "Z9Z9Z9Z9Z9" };

		System.out.println("Custom Base36: " + new BigInteger("Z9Z9Z9Z9Z9", 36));

		for(String accession : tests) {
			try {
				long decoded = decodeBase36custom(accession);
				if(decoded < 0) {
					System.out.println("Decoded value is negative: " + decoded);
				}
				String encoded = encodeBase36(decoded);
				if(!accession.equals(encoded)) {
					System.out.println("Encoded value does not match original: " + accession + " -> " + encoded);
				}
				System.out.println(accession + " -> " + decoded + " (stane u long)");
				System.out.println(accession + " -> " + Long.MAX_VALUE + " (stane u long)");

			} catch(ArithmeticException e) {
				System.out.println(e.getMessage());
			}
		}
	}

}