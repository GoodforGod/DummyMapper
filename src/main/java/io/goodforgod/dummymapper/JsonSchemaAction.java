package io.goodforgod.dummymapper;

import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.mapper.impl.JsonSchemaMapper;
import io.goodforgod.dummymapper.ui.config.JsonSchemaConfig;
import org.jetbrains.annotations.NotNull;

/**
 * Entry-point for JSON mapper plugin
 *
 * @author GoodforGod
 * @since 17.11.2019
 */
public class JsonSchemaAction extends MapperAction<JsonSchemaConfig> {

    private final JsonSchemaMapper mapper = new JsonSchemaMapper();

    @NotNull
    @Override
    public JsonSchemaMapper getMapper() {
        return mapper;
    }

    @Override
    protected String configDialogTitle() {
        return "Json Schema Options";
    }

    @NotNull
    public String format() {
        return "JSON Schema";
    }
}
