package com.extremum.common.mapper;

import com.extremum.common.collection.CollectionDescriptor;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.collection.serde.CollectionDescriptorDeserializer;
import com.extremum.common.descriptor.serde.DescriptorDeserializer;
import com.extremum.common.deserializers.*;
import com.extremum.common.serializers.DisplaySerializer;
import com.extremum.common.serializers.DurationVariativeValueSerializer;
import com.extremum.common.serializers.IdListOrObjectListStructSerializer;
import com.extremum.common.serializers.IdOrObjectStructSerializer;
import com.extremum.common.serializers.IntegerOrStringSerializer;
import com.extremum.common.serializers.IntegerRangeOrValueSerializer;
import com.extremum.common.serializers.MultilingualObjectSerializer;
import com.extremum.common.stucts.Display;
import com.extremum.common.stucts.DurationVariativeValue;
import com.extremum.common.stucts.IdListOrObjectListStruct;
import com.extremum.common.stucts.IdOrObjectStruct;
import com.extremum.common.stucts.IntegerOrString;
import com.extremum.common.stucts.IntegerRangeOrValue;
import com.extremum.common.stucts.MultilingualObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;


/**
 * Public object mapper for clients.
 */
public class JsonObjectMapper extends BasicJsonObjectMapper {
    private final Level level;
    private final MapperDependencies collectionDescriptorTransfigurationDependencies;

    private JsonObjectMapper(Level level,
            MapperDependencies collectionDescriptorsTransfigurationDependencies) {
        if (level.hasCollectionDescriptors()) {
            makeSureDependenciesArePresent(collectionDescriptorsTransfigurationDependencies);
        }
        this.level = level;
        this.collectionDescriptorTransfigurationDependencies = collectionDescriptorsTransfigurationDependencies;
    }

    private void makeSureDependenciesArePresent(MapperDependencies dependenciesToCheck) {
        if (dependenciesToCheck == null) {
            throw new IllegalStateException(
                    "Descriptor collections transfiguration is enabled but dependencies are null");
        }
    }

    public static JsonObjectMapper createWithoutDescriptorTransfiguration() {
        JsonObjectMapper mapper = new JsonObjectMapper(Level.BASIC, null);
        mapper.configure();
        return mapper;
    }

    public static JsonObjectMapper createWithDescriptors() {
        JsonObjectMapper mapper = new JsonObjectMapper(Level.DESCRIPTORS, null);
        mapper.configure();
        return mapper;
    }

    public static JsonObjectMapper createWithCollectionDescriptors(MapperDependencies dependencies) {
        JsonObjectMapper mapper = new JsonObjectMapper(Level.COLLECTION_DESCRIPTORS, dependencies);
        mapper.configure();
        return mapper;
    }

    @Override
    public ObjectMapper copy() {
        return JsonObjectMapper.createWithoutDescriptorTransfiguration();
    }

    /**
     * Masking descriptor details as if it were a plain string
     */
    @Override
    protected SimpleModule createCustomModule() {
        SimpleModule module = super.createCustomModule();

        if (level.hasDescriptors()) {
            module.addSerializer(Descriptor.class, new ToStringSerializer());
            module.addDeserializer(Descriptor.class, new DescriptorDeserializer());
        }
        if (level.hasCollectionDescriptors()) {
            module.addSerializer(CollectionDescriptor.class, new ToStringSerializer());
            module.addDeserializer(CollectionDescriptor.class, new CollectionDescriptorDeserializer(
                    collectionDescriptorTransfigurationDependencies.collectionDescriptorService()));
        }

        module.addSerializer(MultilingualObject.class, new MultilingualObjectSerializer());
        module.addDeserializer(MultilingualObject.class, new MultilingualObjectDeserializer());

        module.addSerializer(IdListOrObjectListStruct.class, new IdListOrObjectListStructSerializer());

        module.addDeserializer(IntegerRangeOrValue.class, new IntegerRangeOrValueDeserializer());
        module.addSerializer(IntegerRangeOrValue.class, new IntegerRangeOrValueSerializer());

        module.addDeserializer(DurationVariativeValue.class, new DurationVariativeValueDeserializer());
        module.addSerializer(DurationVariativeValue.class, new DurationVariativeValueSerializer());

        module.addDeserializer(Display.class, new DisplayDeserializer(this));
        module.addSerializer(Display.class, new DisplaySerializer());

        module.addSerializer(IntegerOrString.class, new IntegerOrStringSerializer());
        module.addDeserializer(IntegerOrString.class, new IntegerOrStringDeserializer());

        module.addSerializer(IdOrObjectStruct.class, new IdOrObjectStructSerializer(this));

        return module;
    }

    private enum Level {
        BASIC,
        DESCRIPTORS,
        COLLECTION_DESCRIPTORS;

        boolean hasDescriptors() {
            return this == DESCRIPTORS || this == COLLECTION_DESCRIPTORS;
        }

        boolean hasCollectionDescriptors() {
            return this == COLLECTION_DESCRIPTORS;
        }
    }
}