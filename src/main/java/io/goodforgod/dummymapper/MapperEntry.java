package io.goodforgod.dummymapper;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.psi.PsiJavaFile;
import io.dummymaker.util.StringUtils;
import io.goodforgod.dummymapper.error.MapperException;
import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.util.IdeaUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Mapper entry point base implementation class
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.5.2020
 */
public abstract class MapperEntry extends AnAction {

    public MapperEntry() {
        this(AllIcons.Actions.DiffWithClipboard);
    }

    public MapperEntry(@NotNull Icon icon) {
        super(icon);
    }

    @NotNull
    public abstract IMapper getMapper();

    @NotNull
    public abstract String successMessage();

    @NotNull
    public abstract String emptyResultMessage();

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        try {
            final PsiJavaFile file = IdeaUtils.getFileFromAction(event)
                    .orElseThrow(() -> new IllegalArgumentException("File is not Java File!"));

            final String json = getMapper().map(file);
            if (StringUtils.isEmpty(json)) {
                PopupUtil.showBalloonForActiveComponent(emptyResultMessage(), MessageType.WARNING);
                return;
            }

            IdeaUtils.copyToClipboard(json);
            PopupUtil.showBalloonForActiveComponent(successMessage(), MessageType.INFO);
        } catch (MapperException e) {
            PopupUtil.showBalloonForActiveComponent(e.getMessage(), MessageType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            PopupUtil.showBalloonForActiveComponent(e.getMessage(), MessageType.ERROR);
        }
    }
}
