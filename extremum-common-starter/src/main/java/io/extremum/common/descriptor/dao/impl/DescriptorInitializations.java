package io.extremum.common.descriptor.dao.impl;

import io.extremum.sharedmodels.descriptor.Descriptor;

import java.time.ZonedDateTime;

class DescriptorInitializations {
    void fillCreatedAndModifiedDatesManuallyToHaveFullyFilledObjectInRedis(Descriptor descriptor) {
        ZonedDateTime now = ZonedDateTime.now();
        if (descriptor.getCreated() == null) {
            descriptor.setCreated(now);
        }
        if (descriptor.getModified() == null) {
            descriptor.setModified(now);
        }
    }

    boolean shouldInitializeVersionManually(Descriptor descriptor) {
        boolean initializeVersionManually = false;
        if (descriptor.getVersion() == null) {
            initializeVersionManually = true;
        }
        return initializeVersionManually;
    }

    void fillVersionIfNeededToHaveFullyFilledObjectInRedis(Descriptor descriptor, boolean initializeVersionManually) {
        if (initializeVersionManually) {
            descriptor.setVersion(0L);
        }
    }

    void removeVersionIfItWasSetManually(Descriptor descriptor, boolean initializeVersionManually) {
        if (initializeVersionManually) {
            descriptor.setVersion(null);
        }
    }
}
