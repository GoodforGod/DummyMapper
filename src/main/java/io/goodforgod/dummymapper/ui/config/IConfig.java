package io.goodforgod.dummymapper.ui.config;


import java.util.Collection;
import javax.annotation.Nullable;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;


/**
 * @author Anton Kurako (GoodforGod)
 * @since 23.5.2020
 */
public interface IConfig {

    void set(@NotNull String key, @NotNull Object value);

    @Nullable
    String get(@NotNull String key);

    @NotNull
    Collection<JComponent> getComponents();
}
