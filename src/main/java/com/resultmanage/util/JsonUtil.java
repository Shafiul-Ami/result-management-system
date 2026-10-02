package com.resultmanage.util;

public final class JsonUtil {

    private JsonUtil() {
    }

    /** Returns the value as a JSON string: "text" (escaped), or null. */
    public static String str(Object value) {
        if (value == null) {
            return "null";
        }
        String s = value.toString();
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.append('"').toString();
    }
}
