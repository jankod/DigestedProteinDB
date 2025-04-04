package hr.pbf.digestdb.demo;

import java.math.BigInteger;

public class ManualBase36 {
    public static BigInteger decodeBase36(String accession) {
        BigInteger result = BigInteger.ZERO;
        for (char c : accession.toCharArray()) {
            result = result.multiply(BigInteger.valueOf(36));
            int digit = (c >= '0' && c <= '9') ? (c - '0') : (c - 'A' + 10);
            result = result.add(BigInteger.valueOf(digit));
        }
        return result;
    }

    public static void main(String[] args) {
        String accession = "Z9Z9Z9Z9Z9";
        BigInteger value = decodeBase36(accession);
        System.out.println("Točna vrijednost: " + value);
    }
}