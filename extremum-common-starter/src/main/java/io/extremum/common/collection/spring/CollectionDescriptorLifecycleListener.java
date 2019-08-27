package io.extremum.common.collection.spring;

import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

/**
 * @author rpuch
 */
public class CollectionDescriptorLifecycleListener extends AbstractMongoEventListener<CollectionDescriptor> {
    @Override
    public void onBeforeConvert(BeforeConvertEvent<CollectionDescriptor> event) {
        super.onBeforeConvert(event);

        CollectionDescriptor collectionDescriptor = event.getSource();

        collectionDescriptor.refreshCoordinatesString();
    }
}
