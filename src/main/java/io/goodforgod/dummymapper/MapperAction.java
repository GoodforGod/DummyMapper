package io.goodforgod.dummymapper;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.psi.PsiJavaFile;
import io.dummymaker.util.StringUtils;
import io.goodforgod.dummymapper.error.JavaFileException;
import io.goodforgod.dummymapper.error.MapperException;
import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.ui.ConfigDialog;
import io.goodforgod.dummymapper.ui.config.IConfig;
import io.goodforgod.dummymapper.util.IdeaUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.StringJoiner;

/**
 * Mapper entry point base implementation class
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.5.2020
 */
public abstract class MapperAction<T extends IConfig> extends AnAction {

    public MapperAction() {
        super();
    }

    public MapperAction(@NotNull Icon icon) {
        super(icon);
    }

    @NotNull
    public abstract IMapper<T> getMapper();

    @NotNull
    protected abstract String format();

    @NotNull
    protected String successMessage() {
        return format() + " copied to clipboard";
    }

    @NotNull
    protected String emptyResultMessage() {
        return "No fields found to map for " + format();
    }

    protected String configDialogTitle() {
        return "Options";
    }

    @Nullable
    protected T getConfig() {
        return null;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        try {
            final PsiJavaFile file = IdeaUtils.getFileFromAction(event)
                    .orElseThrow(JavaFileException::new);

            final T config = getConfig();
            if (config != null) {
                final Project project = event.getProject();
                final Collection<JComponent> components = config.getComponents();
                final ConfigDialog dialog = new ConfigDialog(project, configDialogTitle(), components);
                dialog.show();
                if (dialog.getExitCode() == 1)
                    return;

                dialog.disposeIfNeeded();
            }

            final String json = getMapper().map(file, config);
            if (StringUtils.isEmpty(json)) {
                PopupUtil.showBalloonForActiveFrame(emptyResultMessage(), MessageType.WARNING);
                return;
            }

            IdeaUtils.copyToClipboard(json);
            PopupUtil.showBalloonForActiveFrame(successMessage(), MessageType.INFO);
        } catch (MapperException | JavaFileException e) {
            if (StringUtils.isEmpty(e.getMessage()))
                throw new IllegalArgumentException("Unknown error occurred", e);

            PopupUtil.showBalloonForActiveFrame(e.getMessage(), MessageType.WARNING);
        } catch (Exception e) {
            e.printStackTrace();
            final String title = "Failed mapping to " + format();
            final StringJoiner joiner = new StringJoiner("\n");
            joiner.add("There was an error mapping file to " + format() + ".\n");
            joiner.add("Please report <a href=\"https://github.com/GoodforGod/DummyMapper/issues\">this issue here</a>.\n");
            joiner.add("Error message: " + e.getMessage());
            joiner.add("Stacktrace: " + getStackTrace(e));

            Notifications.Bus.notify(new Notification("Mapping Error", title, joiner.toString(), NotificationType.ERROR));
        }
    }

    protected String getStackTrace(Exception e) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
