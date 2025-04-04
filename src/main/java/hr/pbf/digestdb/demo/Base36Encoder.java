package hr.pbf.digestdb.demo;

import java.math.BigInteger;

public class Base36Encoder {

    public static long encodeBase36(String input) {
        BigInteger result = BigInteger.ZERO;
        BigInteger power = BigInteger.ONE;
        BigInteger base = BigInteger.valueOf(36);

        for (int i = input.length() - 1; i >= 0; i--) {
            char c = input.charAt(i);
            int digit = Character.digit(c, 36);
            if (digit == -1) {
                throw new IllegalArgumentException("Invalid base-36 character: " + c);
            }
            result = result.add(BigInteger.valueOf(digit).multiply(power));
            power = power.multiply(base);
        }
        return result.longValue();
    }

    public static void main(String[] args) {
        String input = "Z9Z9Z9Z9Z9";
        long encodedValue = encodeBase36(input);
        System.out.println("Encoded value: " + encodedValue);
    }
}