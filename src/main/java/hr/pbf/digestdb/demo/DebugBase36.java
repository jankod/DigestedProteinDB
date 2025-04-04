package hr.pbf.digestdb.demo;

import java.math.BigInteger;

public class DebugBase36 {
    public static BigInteger decodeBase36(String accession) {
        BigInteger result = BigInteger.ZERO;
        System.out.println("Dekodiranje: " + accession);
        int position = 0;
        for (char c : accession.toCharArray()) {
            result = result.multiply(BigInteger.valueOf(36));
            int digit = (c >= '0' && c <= '9') ? (c - '0') : (c - 'A' + 10);
            result = result.add(BigInteger.valueOf(digit));
            System.out.println("Pozicija " + position + ": Znak = " + c + ", Vrijednost = " + digit + ", Rezultat = " + result);
            position++;
        }
        return result;
    }

    public static void main(String[] args) {
        String accession = "Z9Z9Z9Z9Z9";
        BigInteger value = decodeBase36(accession);
        System.out.println("Konačna vrijednost: " + value);
        BigInteger bigIntValue = new BigInteger(accession, 36);
        System.out.println("BigInteger vrijednost: " + bigIntValue);
    }
}