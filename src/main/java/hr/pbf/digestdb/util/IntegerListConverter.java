package hr.pbf.digestdb.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class IntegerListConverter {

    public static byte[] toByteArray(List<Integer> intList) {
        if (intList == null || intList.isEmpty()) {
            return new byte[0];
        }

        // Svaki Integer zauzima 4 bajta
        ByteBuffer byteBuffer = ByteBuffer.allocate(intList.size() * 4);

        // Opcionalno: Postavi redoslijed bajtova (npr. LITTLE_ENDIAN ili BIG_ENDIAN)
        // Ako ćeš pohraniti podatke na istom sustavu ili ako želiš interoperabilnost
        // s drugim jezicima/platformama, ovo je bitno.
        // Npr. byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        // Default je BIG_ENDIAN na većini JVM-ova, ali je dobro biti eksplicitan.
        byteBuffer.order(ByteOrder.BIG_ENDIAN); // Standardni mrežni redoslijed

        for (Integer i : intList) {
            byteBuffer.putInt(i);
        }

        return byteBuffer.array();
    }

    public static List<Integer> toIntegerList(byte[] byteArray) {
        if (byteArray == null || byteArray.length == 0) {
            return new ArrayList<>();
        }

        if (byteArray.length % 4 != 0) {
            throw new IllegalArgumentException("Byte array length must be a multiple of 4.");
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
        byteBuffer.order(ByteOrder.BIG_ENDIAN); // Koristi isti redoslijed bajtova kao kod spremanja

        List<Integer> intList = new ArrayList<>(byteArray.length / 4);

        while (byteBuffer.hasRemaining()) {
            intList.add(byteBuffer.getInt());
        }

        return intList;
    }

    public static void main(String[] args) {
        List<Integer> originalList = new ArrayList<>();
        originalList.add(10);
        originalList.add(255);
        originalList.add(-1000);
        originalList.add(Integer.MAX_VALUE);
        originalList.add(Integer.MIN_VALUE);

        System.out.println("Original List: " + originalList);

        byte[] byteArray = toByteArray(originalList);
        System.out.println("Byte Array Length: " + byteArray.length);
        // Ispis bajtova (samo za provjeru, može biti nečitljivo)
        // System.out.println("Byte Array: " + java.util.Arrays.toString(byteArray));

        List<Integer> retrievedList = toIntegerList(byteArray);
        System.out.println("Retrieved List: " + retrievedList);

        System.out.println("Lists are equal: " + originalList.equals(retrievedList));
    }
}
