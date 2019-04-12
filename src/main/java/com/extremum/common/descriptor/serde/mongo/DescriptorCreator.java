package com.extremum.common.descriptor.serde.mongo;

import com.extremum.common.descriptor.Descriptor;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.commons.lang3.EnumUtils;
import org.mongodb.morphia.mapping.DefaultCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DescriptorCreator extends DefaultCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DescriptorCreator.class);

    static Descriptor createDescriptorFromDBObject(Object fromDBObject) {
        if (!(fromDBObject instanceof BasicDBObject)) {
            throw new IllegalStateException("Object expected; got: " + fromDBObject);
        }

        BasicDBObject descriptorDbObject = (BasicDBObject) fromDBObject;

        String rawType = descriptorDbObject.getString("type");
        Descriptor.StorageType storageType = EnumUtils.getEnum(Descriptor.StorageType.class, rawType);
        if (storageType == null) {
            LOGGER.warn("Unknown descriptor type: {}", rawType);
        }

        return new Descriptor(
                descriptorDbObject.getString("_id"),
                descriptorDbObject.getString("internalId"),
                descriptorDbObject.getString("model"),
                storageType
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createInstance(Class<T> clazz, DBObject dbObj) {
        if (!Descriptor.class.equals(clazz)) {
            return super.createInstance(clazz, dbObj);
        }
        return (T) createDescriptorFromDBObject(dbObj);
    }
}
