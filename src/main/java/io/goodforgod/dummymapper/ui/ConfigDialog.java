package io.goodforgod.dummymapper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 7.5.2020
 */
public class ConfigDialog extends DialogWrapper {

    private final Collection<JComponent> components;

    public ConfigDialog(@Nullable Project project, String title, Collection<JComponent> components) {
        super(project, true, IdeModalityType.PROJECT);
        this.components = components;
        setTitle(title);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setPreferredSize(new Dimension(350, 250));
        components.forEach(panel::add);
        return panel;
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "options-dialog";
    }
}