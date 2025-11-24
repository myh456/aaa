package org.example.inspection;

import java.util.List;
import java.util.Map;

public class TestUtil {
    public static void printObj(Object map, String space) {
        if (map instanceof List) {
            for (Object item : (List<?>) map) {
                printObj(item, space + "    ");
            }
        } else if (map instanceof Map) {
            for (Map.Entry entry : ((Map<?, ?>) map).entrySet()) {
                System.out.println(space + entry.getKey() + ": ");
                printObj(entry.getValue(), space + "    ");
            }
        } else {
            System.out.println(space + map);
        }
    }
}
