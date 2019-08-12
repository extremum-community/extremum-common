package io.extremum.common.collection.serde;

import io.extremum.common.collection.CollectionDescriptor;
import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.descriptor.exceptions.CollectionDescriptorNotFoundException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

import java.io.IOException;

public class CollectionDescriptorDeserializer extends StdScalarDeserializer<CollectionDescriptor> {
    private final CollectionDescriptorService collectionDescriptorService;

    public CollectionDescriptorDeserializer(CollectionDescriptorService collectionDescriptorService) {
        super(CollectionDescriptor.class);
        this.collectionDescriptorService = collectionDescriptorService;
    }

    @Override
    public CollectionDescriptor deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String externalId = _parseString(p, ctxt);
        return collectionDescriptorService.retrieveByExternalId(externalId)
                .orElseThrow(() -> new CollectionDescriptorNotFoundException(
                        String.format("No collection descriptor was found by external ID '%s'", externalId)));
    }
}
