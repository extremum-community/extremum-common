package com.extremum.common.descriptor.serde.mongo;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.impl.MongoDescriptorFactory;
import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;


public class DescriptorStringConverter extends TypeConverter implements SimpleValueConverter {

    public DescriptorStringConverter() {
        super(Descriptor.class);
    }

    @Override
    public Object decode(Class<?> targetClass, Object fromDBObject, MappedField optionalExtraInfo) {
        if (targetClass.equals(Descriptor.class)) {
            return MongoDescriptorFactory.fromInternalId(fromDBObject.toString());
        } else {
            throw new IllegalArgumentException("Unexpected class " + targetClass);
        }
    }

    @Override
    public Object encode(Object value, MappedField optionalExtraInfo) {
        if (value == null) {
            return null;
        }
        if (value instanceof Descriptor) {
            return ((Descriptor) value).getInternalId();
        } else {
            throw new IllegalArgumentException("Unexpected class " + value.getClass());
        }
    }
}