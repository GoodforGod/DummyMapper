package io.goodforgod.dummymapper;


import io.goodforgod.dummymapper.mapper.IMapper;
import io.goodforgod.dummymapper.mapper.impl.AvroApacheMapper;
import org.jetbrains.annotations.NotNull;


/**
 * Entry-point for {@link AvroApacheMapper}
 *
 * @author GoodforGod
 * @since 17.11.2019
 */
public class AvroSchemaApacheAction extends MapperAction {

    private final IMapper mapper = new AvroApacheMapper();

    @NotNull
    @Override
    public IMapper getMapper() {
        return mapper;
    }

    @NotNull
    @Override
    protected String format() {
        return "AVRO Schema (Apache)";
    }
}
