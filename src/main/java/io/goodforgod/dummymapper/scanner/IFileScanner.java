package io.goodforgod.dummymapper.scanner;

import com.intellij.psi.PsiFile;
import io.goodforgod.dummymapper.marker.RawMarker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link PsiFile} scanner interface that scans class file and build tree structure of such class
 * file
 *
 * @author Anton Kurako (GoodforGod)
 * @since 15.8.2020
 */
public interface IFileScanner {

    @NotNull
    RawMarker scan(@Nullable PsiFile file);
}
