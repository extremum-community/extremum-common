package com.extremum.jpa.services.lifecycle;

import com.extremum.common.models.BasicModel;
import com.extremum.common.utils.ModelUtils;
import com.extremum.jpa.facilities.PostgresqlDescriptorFacilities;
import com.extremum.jpa.facilities.StaticPostgresqlDescriptorFacilitiesAccessor;
import com.extremum.sharedmodels.descriptor.Descriptor;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import java.util.UUID;

/**
 * @author rpuch
 */
public class JpaCommonModelLifecycleListener {

    @PrePersist
    public void fillRequiredFields(BasicModel<UUID> model) {
        ensureFacilitiesAreAvailable();

        final boolean internalIdGiven = model.getId() != null;
        final boolean uuidGiven = model.getUuid() != null;

        if (uuidGiven && !internalIdGiven) {
            model.setId(getInternalIdFromDescriptor(model));
        } else if (!uuidGiven && internalIdGiven) {
            Descriptor descriptor = createAndSaveDescriptorWithGivenInternalId(model.getId(), model);
            model.setUuid(descriptor);
        } else if (!uuidGiven && !internalIdGiven) {
            Descriptor descriptor = createAndSaveDescriptorWithGivenInternalId(UUID.randomUUID(), model);
            model.setUuid(descriptor);
            model.setId(getInternalIdFromDescriptor(model));
        }
    }

    private void ensureFacilitiesAreAvailable() {
        if (descriptorFacilities() == null) {
            throw new IllegalStateException("PostgresqlDescriptorFacilities is not available");
        }
    }

    private PostgresqlDescriptorFacilities descriptorFacilities() {
        return StaticPostgresqlDescriptorFacilitiesAccessor.getFacilities();
    }

    private UUID getInternalIdFromDescriptor(BasicModel<UUID> model) {
        return descriptorFacilities().resolve(model.getUuid());
    }

    private Descriptor createAndSaveDescriptorWithGivenInternalId(UUID internalId, BasicModel<UUID> model) {
        String modelName = ModelUtils.getModelName(model);
        return descriptorFacilities().create(internalId, modelName);
    }

    @PostPersist
    public void createDescriptorIfNeeded(BasicModel<UUID> model) {
        ensureFacilitiesAreAvailable();

        if (model.getUuid() == null) {
            model.setUuid(descriptorFacilities().create(model.getId(), ModelUtils.getModelName(model)));
        }
    }

    @PostLoad
    public void onAfterConvert(BasicModel<UUID> model) {
        ensureFacilitiesAreAvailable();

        model.setUuid(descriptorFacilities().fromInternalId(model.getId()));
    }
}
