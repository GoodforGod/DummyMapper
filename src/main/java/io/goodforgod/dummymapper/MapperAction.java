package io.goodforgod.dummymapper;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.psi.PsiClass;
import io.dummymaker.util.StringUtils;
import io.goodforgod.dummymapper.error.MapperException;
import io.goodforgod.dummymapper.error.PsiKindException;
import io.goodforgod.dummymapper.error.UnsupportedPsiFileException;
import io.goodforgod.dummymapper.marker.MarkerMapper;
import io.goodforgod.dummymapper.marker.RawMarker;
import io.goodforgod.dummymapper.service.PsiClassScanner;
import io.goodforgod.dummymapper.ui.ConfigDialog;
import io.goodforgod.dummymapper.ui.config.IConfig;
import io.goodforgod.dummymapper.util.IdeaUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.StringJoiner;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Mapper entry point base implementation class
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.5.2020
 */
public abstract class MapperAction<T extends IConfig> extends AnAction {

    private static final String DISPLAY_GROUP_ERROR = "DummyMapping Plugin Errors";
    private static final String ISSUE_URL = "https://github.com/GoodforGod/DummyMapper/issues";

    public MapperAction() {
        super();
    }

    public MapperAction(@NotNull Icon icon) {
        super(icon);
    }

    @NotNull
    public abstract MarkerMapper<T> getMapper();

    /**
     * @return format as string in which mapper is converting (like JSON)
     */
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
        return format() + " Options";
    }

    /**
     * @return configuration for mapper
     */
    @Nullable
    protected T getConfig() {
        return null;
    }

    /**
     * Performs mapping action for override mapper
     * 
     * @param event from IDE
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        try {
            final PsiClass psiClass = IdeaUtils.getPsiClassFromAction(event)
                    .orElseGet(() -> IdeaUtils.getFileFromAction(event)
                            .filter(file -> file.getClasses().length != 0)
                            .map(file -> file.getClasses()[0])
                            .orElseThrow(UnsupportedPsiFileException::new));

            final T config = getConfig();
            if (config != null) {
                final Project project = event.getProject();
                final Collection<JComponent> components = config.getComponents();
                final ConfigDialog dialog = new ConfigDialog(project, configDialogTitle(), components);
                dialog.show();
                if (dialog.getExitCode() == 1) {
                    return;
                }

                dialog.disposeIfNeeded();
            }

            final RawMarker marker = new PsiClassScanner().scan(psiClass);
            final String json = getMapper().map(marker, config);
            if (StringUtils.isEmpty(json)) {
                PopupUtil.showBalloonForActiveFrame(emptyResultMessage(), MessageType.WARNING);
                return;
            }

            IdeaUtils.copyToClipboard(json);
            PopupUtil.showBalloonForActiveFrame(successMessage(), MessageType.INFO);
        } catch (MapperException | UnsupportedPsiFileException | PsiKindException e) {
            if (StringUtils.isEmpty(e.getMessage())) {
                throw new IllegalArgumentException("Unknown error occurred", e);
            }

            PopupUtil.showBalloonForActiveFrame(e.getMessage(), MessageType.WARNING);
        } catch (Exception e) {
            final StringJoiner joiner = new StringJoiner("\n");
            joiner.add("There was an error mapping file to " + format() + ".");
            joiner.add("Please report this issue here: <b><a href=\"" + ISSUE_URL + "\">" + ISSUE_URL + "</a></b>");
            joiner.add("Stacktrace: " + getStackTrace(e));

            final String title = "Failed mapping to " + format();
            Notifications.Bus.notify(new Notification(DISPLAY_GROUP_ERROR, title, joiner.toString(), NotificationType.ERROR));
        }
    }

    protected String getStackTrace(Exception e) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
