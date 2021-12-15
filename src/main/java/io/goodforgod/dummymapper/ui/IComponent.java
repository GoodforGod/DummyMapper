package io.goodforgod.dummymapper.ui;


import io.goodforgod.dummymapper.ui.config.IConfig;
import javax.swing.*;


/**
 * @author Anton Kurako (GoodforGod)
 * @since 12.6.2020
 */
public interface IComponent {

    JComponent build(IConfig config);
}
