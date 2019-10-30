package io.extremum.mongo.facilities;

import io.extremum.sharedmodels.descriptor.Descriptor;

class DescriptorReadinessValidation {
    void validateDescriptorIsNotReady(String descriptorId, Descriptor descriptor) {
        if (descriptor.getReadiness() == Descriptor.Readiness.READY) {
            throw new IllegalStateException(
                    "The descriptor with external ID '" + descriptorId + "' is already ready");
        }
    }
}
