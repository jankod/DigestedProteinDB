package hr.pbf.digestdb.util;

import lombok.Getter;

@Getter
public class LongCounter {
    private long count = 0;

    public void increment() {
        count++;
    }

    public long get() {
        return count;
    }
}
