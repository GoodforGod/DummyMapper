package io.goodforgod.dummymapper.ui;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 23.5.2020
 */
public class JsonSchemaDialog extends ConfigDialog {

    public JsonSchemaDialog(@Nullable Project project) {
        super(project, "Json Schema Options");
    }
}
