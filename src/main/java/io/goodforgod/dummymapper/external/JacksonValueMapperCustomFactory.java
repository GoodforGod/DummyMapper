package io.goodforgod.dummymapper.external;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.goodforgod.dummymapper.error.ExternalException;
import io.leangen.graphql.execution.GlobalEnvironment;
import io.leangen.graphql.metadata.strategy.value.jackson.JacksonValueMapper;
import io.leangen.graphql.metadata.strategy.value.jackson.JacksonValueMapperFactory;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;


/**
 * @author Anton Kurako (GoodforGod)
 * @since 13.6.2020
 */
public class JacksonValueMapperCustomFactory extends JacksonValueMapperFactory {

    @Override
    public JacksonValueMapper getValueMapper(Map<Class, List<Class<?>>> concreteSubTypes, GlobalEnvironment environment) {
        try {
            final Constructor<?> constructor = JacksonValueMapper.class.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            return (JacksonValueMapper) constructor.newInstance(new ObjectMapper());
        } catch (Exception e) {
            throw new ExternalException(e);
        }
    }
}
