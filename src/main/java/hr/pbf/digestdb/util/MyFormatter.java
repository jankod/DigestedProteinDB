package hr.pbf.digestdb.util;

import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.Map;

public class MyFormatter {
    public static String format(String template, String... paramValue) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < paramValue.length; i++) {
            if (i + 1 >= paramValue.length) {
                throw new IllegalArgumentException("Odd number of parameters paramValue");
            }
            map.put(paramValue[i], paramValue[i + 1]);
            i++;
        }
        StringSubstitutor sub = new StringSubstitutor(map);
        return sub.replace(template);
    }
}
