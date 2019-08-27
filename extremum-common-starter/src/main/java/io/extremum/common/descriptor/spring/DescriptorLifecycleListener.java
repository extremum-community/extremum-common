package io.extremum.common.descriptor.spring;

import io.extremum.sharedmodels.descriptor.Descriptor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

/**
 * @author rpuch
 */
public class DescriptorLifecycleListener extends AbstractMongoEventListener<Descriptor> {
    @Override
    public void onBeforeConvert(BeforeConvertEvent<Descriptor> event) {
        super.onBeforeConvert(event);

        Descriptor descriptor = event.getSource();

        if (descriptor.getType() == Descriptor.Type.COLLECTION && descriptor.getCollection() != null) {
            descriptor.getCollection().refreshCoordinatesString();
        }
    }
}
