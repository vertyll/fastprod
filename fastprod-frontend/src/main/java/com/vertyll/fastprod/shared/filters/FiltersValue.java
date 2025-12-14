package com.vertyll.fastprod.shared.filters;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FiltersValue {
    private final Map<String, Object> values = new HashMap<>();

    public static FiltersValue empty() {
        return new FiltersValue();
    }

    public void set(String key, Object value) {
        switch (value) {
            case null -> values.remove(key);
            case String s when s.isBlank() -> values.remove(key);
            case Collection<?> c when c.isEmpty() -> values.remove(key);
            default -> values.put(key, value);
        }
    }

    @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
    public <T> T get(String key) {
        return (T) values.get(key);
    }

    public Map<String, Object> asMap() {
        return Collections.unmodifiableMap(values);
    }

    public String toQueryString() {
        if (values.isEmpty()) return "";
        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, Object> e : values.entrySet()) {
            String k = url(e.getKey());
            Object v = e.getValue();
            if (v instanceof Collection<?> col) {
                String joined = String.join(
                        ",",
                        col.stream().map(String::valueOf).toList()
                );
                parts.add(k + "=" + url(joined));
            } else {
                parts.add(k + "=" + url(String.valueOf(v)));
            }
        }
        return String.join("&", parts);
    }

    private static String url(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
