package io.goodforgod.dummymapper.mapper.impl;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.intellij.psi.PsiJavaFile;
import io.goodforgod.dummymapper.filter.IFilter;
import io.goodforgod.dummymapper.filter.impl.EmptyMarkerFilter;
import io.goodforgod.dummymapper.filter.impl.ExcludeSetterAnnotationFilter;
import io.goodforgod.dummymapper.filter.impl.GenEnumAnnotationFilter;
import java.text.SimpleDateFormat;

/**
 * Maps instance of {@link PsiJavaFile} to JSON format as example
 *
 * @author Anton Kurako (GoodforGod)
 * @since 28.4.2020
 */
abstract class AbstractJsonJacksonMapper {

    protected final ObjectMapper mapper;

    protected final IFilter emptyFilter;
    protected final IFilter annotationFilter;
    protected final IFilter annotationEnumFilter;

    protected AbstractJsonJacksonMapper() {
        this.annotationEnumFilter = new GenEnumAnnotationFilter();
        this.annotationFilter = new ExcludeSetterAnnotationFilter();
        this.emptyFilter = new EmptyMarkerFilter();

        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        this.mapper.registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
        this.mapper.setConfig(mapper.getSerializationConfig()
                .with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .with(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .without(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }
}
