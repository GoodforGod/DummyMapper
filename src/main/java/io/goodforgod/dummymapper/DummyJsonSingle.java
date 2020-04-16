package io.goodforgod.dummymapper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import io.dummymaker.annotation.special.GenAuto;
import io.dummymaker.factory.impl.GenFactory;
import io.dummymaker.generator.IGenerator;
import io.dummymaker.model.GenRule;
import io.dummymaker.model.GenRules;
import io.goodforgod.dummymapper.model.EnumMarker;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Entry-point fpr plugin
 *
 * @author GoodforGod
 * @since 17.11.2019
 */
public class DummyJsonSingle extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        try {
            final Editor editor = event.getData(CommonDataKeys.EDITOR);
            final PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);

            final PsiElement elementAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
            final PsiDirectory directory = elementAt.getContainingFile().getContainingDirectory();

            final JavaFileScanner scanner = new JavaFileScanner();
            final Map<String, Object> scan = scanner.scan((PsiJavaFile) psiFile);

            final Optional<Class> build = ClassFactory.build(scan);
            if (!build.isPresent())
                return;

            final Class target = build.get();
            final GenRule rule = GenRule.auto(target, GenAuto.MAX);
            scan.forEach((k, v) -> {
                if (v instanceof EnumMarker) {
                    final IGenerator generator = () -> {
                        final List<String> values = ((EnumMarker) v).getValues();
                        return values.get(ThreadLocalRandom.current().nextInt(values.size()));
                    };

                    rule.add(generator, k);
                }
            });

            final GenFactory factory = new GenFactory(GenRules.of(rule));
            final Object o = factory.build(target);

            final String dirPath = directory.toString().replace("PsiDirectory:", "file:/");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
