package hr.pbf.digestdb.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyFormatterTest {

    @Test
    void test1() {
        String result1 = MyFormatter.format("Hello ${name}", "name", "John");
        assertEquals("Hello John", result1);

        String result2 = MyFormatter.format("pero");
        assertEquals("pero", result2);

        assertThrows(IllegalArgumentException.class, () -> {
            MyFormatter.format("Hello ${name} age is ${age}", "name", "John", "age");
        });

    }
}
