package hr.pbf.digestdb.util;

import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.Map;

public class MyFormatter {

    private final String template;

    private final Map<String, String> params = new HashMap<>();

    public MyFormatter(String template) {
        this.template = template;
    }
    public MyFormatter param(String key, String value) {
        params.put(key, value);
        return this;
    }

    public String format() {
        StringSubstitutor sub = new StringSubstitutor(params);
        return sub.replace(template);
    }

    /**
     * Format string with parameters, odd number of parameters is not allowed.
     * Use StringSubstitutor.
     * @param template template string with parameters in form of {key}.
     * @param paramValue array of key, value pairs.
     */
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
