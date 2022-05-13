package io.goodforgod.dummymapper.ui.config;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 6.6.2020
 */
public abstract class AbstractConfig implements IConfig {

    protected final Map<String, String> config = new HashMap<>();

    public void set(@NotNull String key, @NotNull Object value) {
        config.put(key, String.valueOf(value));
    }

    public String get(@NotNull String key) {
        return config.get(key);
    }
}
