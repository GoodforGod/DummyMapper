package io.goodforgod.dummymapper.mapper;

import com.intellij.psi.PsiJavaFile;
import io.goodforgod.dummymapper.ui.config.IConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Map contract to map instance of {@link PsiJavaFile} to format as string value
 *
 * @author Anton Kurako (GoodforGod)
 * @since 28.4.2020
 */
public interface IMapper<T extends IConfig> {

    @NotNull
    default String map(@NotNull PsiJavaFile file) {
        return map(file, null);
    }

    /**
     * @param file to map
     * @param config for specific mapper
     * @return file mapped to its type as string value
     * @throws io.goodforgod.dummymapper.error.MapperException in case of map exception
     */
    @NotNull
    String map(@NotNull PsiJavaFile file, @Nullable T config);
}
