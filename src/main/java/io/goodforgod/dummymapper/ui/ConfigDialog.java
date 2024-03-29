package io.goodforgod.dummymapper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import java.awt.*;
import java.util.Collection;
import javax.swing.*;
import org.jetbrains.annotations.Nullable;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 7.5.2020
 */
public class ConfigDialog extends DialogWrapper {

    private final Collection<JComponent> components;

    public ConfigDialog(@Nullable Project project, String title, Collection<JComponent> components) {
        super(project, false, IdeModalityType.PROJECT);
        this.components = components;
        setTitle(title);
        init();
        setResizable(false);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new GridLayout(0, 1));
        components.forEach(panel::add);
        return panel;
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "options-dialog";
    }
}
