package io.goodforgod.dummymapper.mapper;

import com.intellij.psi.PsiJavaFile;
import org.jetbrains.annotations.NotNull;

/**
 * Map contract to map instance of {@link PsiJavaFile} to format as string value
 *
 * @author Anton Kurako (GoodforGod)
 * @since 28.4.2020
 */
public interface IMapper {

    /**
     * @param file to map
     * @return file mapped to its type as string value
     * @throws io.goodforgod.dummymapper.error.MapperException in case of map exception
     */
    @NotNull
    String map(@NotNull PsiJavaFile file);
}
