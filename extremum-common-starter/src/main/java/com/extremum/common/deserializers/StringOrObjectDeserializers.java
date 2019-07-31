package com.extremum.common.deserializers;

import com.extremum.sharedmodels.basic.StringOrObject;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.ReferenceType;

public class StringOrObjectDeserializers extends Deserializers.Base {
    @Override
    public JsonDeserializer<?> findReferenceDeserializer(ReferenceType type,
                                                         DeserializationConfig config, BeanDescription beanDesc,
                                                         TypeDeserializer contentTypeDeserializer, JsonDeserializer<?> contentDeserializer)
            throws JsonMappingException {
        Class<?> raw = type.getRawClass();
        if (raw == StringOrObject.class)
            return new StringOrObjectDeserializer(type);
        return super.findReferenceDeserializer(type, config, beanDesc, contentTypeDeserializer, contentDeserializer);
    }
}