package io.goodforgod.dummymapper;

import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.mapper.impl.JsonSchemaMapper;
import org.jetbrains.annotations.NotNull;

/**
 * Entry-point for JSON mapper plugin
 *
 * @author GoodforGod
 * @since 17.11.2019
 */
public class JsonMapperEntry extends MapperEntry {

    private final IMapper mapper = new JsonSchemaMapper();

    @NotNull
    @Override
    public IMapper getMapper() {
        return mapper;
    }

    @NotNull
    @Override
    public String successMessage() {
        return "JSON copied to clipboard";
    }

    @NotNull
    @Override
    public String emptyResultMessage() {
        return "No fields found to map for JSON";
    }
}
