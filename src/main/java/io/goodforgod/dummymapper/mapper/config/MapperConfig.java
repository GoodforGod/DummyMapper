package io.goodforgod.dummymapper.mapper.config;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 12.6.2020
 */
public abstract class MapperConfig {

    private final Map<String, String> options = new HashMap<>();

    public Map<String, String> getOptions() {
        return options;
    }

    public MapperConfig set(String key, Object value) {
        options.put(key, String.valueOf(value));
        return this;
    }

    public String get(String key) {
        return options.get(key);
    }
}
