package com.extremum.common.descriptor.serde.mongo;

import com.extremum.common.descriptor.Descriptor;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

public class DescriptorConverter extends TypeConverter {

    public DescriptorConverter() {
        super(Descriptor.class);
    }

    @Override
    public Object decode(Class<?> targetClass, Object fromDBObject, MappedField optionalExtraInfo) {
        if (!Descriptor.class.equals(targetClass)) {
            throw new IllegalStateException(Descriptor.class.getCanonicalName() + " expected; got: " + targetClass);
        }

        return DescriptorCreator.createDescriptorFromDBObject(fromDBObject);
    }

}
