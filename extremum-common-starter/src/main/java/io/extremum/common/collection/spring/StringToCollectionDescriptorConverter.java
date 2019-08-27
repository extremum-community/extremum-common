package io.extremum.common.collection.spring;

import io.extremum.common.collection.service.CollectionDescriptorService;
import io.extremum.common.descriptor.exceptions.CollectionDescriptorNotFoundException;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import org.springframework.core.convert.converter.Converter;

/**
 * @author rpuch
 */
public class StringToCollectionDescriptorConverter implements Converter<String, CollectionDescriptor> {
    private final CollectionDescriptorService collectionDescriptorService;

    public StringToCollectionDescriptorConverter(CollectionDescriptorService collectionDescriptorService) {
        this.collectionDescriptorService = collectionDescriptorService;
    }

    @Override
    public CollectionDescriptor convert(String externalId) {
        // TODO: remove
        throw new UnsupportedOperationException("Please remove this");
//        return collectionDescriptorService.retrieveByExternalId(externalId)
//                .orElseThrow(() -> new CollectionDescriptorNotFoundException(
//                        String.format("No collection descriptor was found by external ID '%s'", externalId)));
    }
}
