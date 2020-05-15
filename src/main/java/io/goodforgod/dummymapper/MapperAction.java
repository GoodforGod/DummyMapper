package io.goodforgod.dummymapper;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.impl.DialogWrapperPeerFactoryImpl;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.psi.PsiJavaFile;
import io.dummymaker.util.StringUtils;
import io.goodforgod.dummymapper.error.JavaFileException;
import io.goodforgod.dummymapper.error.MapperException;
import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.ui.AvroDialogWrapper;
import io.goodforgod.dummymapper.util.IdeaUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Mapper entry point base implementation class
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.5.2020
 */
public abstract class MapperAction extends AnAction {

    public MapperAction() {
        super();
    }

    public MapperAction(@NotNull Icon icon) {
        super(icon);
    }

    @NotNull
    public abstract IMapper getMapper();

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

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        try {
            final PsiJavaFile file = IdeaUtils.getFileFromAction(event)
                    .orElseThrow(JavaFileException::new);

            final String json = getMapper().map(file);
            if (StringUtils.isEmpty(json)) {
                PopupUtil.showBalloonForActiveComponent(emptyResultMessage(), MessageType.WARNING);
                return;
            }

            IdeaUtils.copyToClipboard(json);
            PopupUtil.showBalloonForActiveComponent(successMessage(), MessageType.INFO);

            final Project project = event.getProject();
            final AvroDialogWrapper dialog = new AvroDialogWrapper(project);
            dialog.show();

            final Map<String, Boolean> boxMap = dialog.getCheckBoxMap();
            final String s = boxMap.toString();

        } catch (MapperException | JavaFileException e) {
            if (StringUtils.isEmpty(e.getMessage()))
                throw new IllegalArgumentException("Unknown error occurred", e);

            PopupUtil.showBalloonForActiveComponent(e.getMessage(), MessageType.WARNING);
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
