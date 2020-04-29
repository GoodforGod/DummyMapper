package io.goodforgod.dummymapper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.psi.PsiJavaFile;
import io.dummymaker.util.StringUtils;
import io.goodforgod.dummymapper.error.MapperException;
import io.goodforgod.dummymapper.mapper.impl.JsonMapper;
import io.goodforgod.dummymapper.util.IdeaUtils;

/**
 * Entry-point for JSON mapper plugin
 *
 * @author GoodforGod
 * @since 17.11.2019
 */
public class JsonSingleEntry extends AnAction {

    private final JsonMapper mapper = new JsonMapper();

    @Override
    public void actionPerformed(AnActionEvent event) {
        try {
            final Project currentProject = event.getProject();
            final PsiJavaFile file = IdeaUtils.getFileFromAction(event)
                    .orElseThrow(() -> new IllegalArgumentException("File is not Java File!"));

            final String json = mapper.map(file);
            if (StringUtils.isEmpty(json)) {
                PopupUtil.showBalloonForActiveComponent("No fields found to map for JSON", MessageType.WARNING);
                return;
            }

            IdeaUtils.copyToClipboard(json);
            PopupUtil.showBalloonForActiveComponent("JSON copied to clipboard", MessageType.INFO);
        } catch (MapperException e) {
            PopupUtil.showBalloonForActiveComponent(e.getMessage(), MessageType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            PopupUtil.showBalloonForActiveComponent(e.getMessage(), MessageType.ERROR);
        }
    }
}
