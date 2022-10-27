package io.goodforgod.dummymapper.marker.mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.intellij.psi.PsiJavaFile;
import io.goodforgod.jackson.module.datetime.configuration.DateTimeFormatters;
import io.goodforgod.jackson.module.datetime.configuration.JavaTimeModule;
import io.goodforgod.jackson.module.datetime.configuration.JavaTimeModuleConfiguration;
import java.text.SimpleDateFormat;

/**
 * Maps instance of {@link PsiJavaFile} to JSON format as example
 *
 * @author Anton Kurako (GoodforGod)
 * @since 28.4.2020
 */
final class ObjectMapperUtils {

    private ObjectMapperUtils() {}

    static ObjectMapper getConfigured() {
        return configure(new ObjectMapper());
    }

    static ObjectMapper configure(ObjectMapper mapper) {
        final JavaTimeModule javaTimeModule = JavaTimeModuleConfiguration.ofISO().getModule();

        mapper.setDateFormat(new SimpleDateFormat(DateTimeFormatters.ISO_DATE));
        mapper.registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(javaTimeModule);

        mapper.setConfig(mapper.getDeserializationConfig()
                .with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .with(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE));

        mapper.setConfig(mapper.getSerializationConfig()
                .with(SerializationFeature.INDENT_OUTPUT)
                .with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .with(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .without(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));

        return mapper;
    }
}
