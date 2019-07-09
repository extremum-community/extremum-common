package com.extremum.jpa.services.lifecycle;

import com.extremum.common.models.BasicModel;
import com.extremum.common.utils.ModelUtils;
import com.extremum.jpa.facilities.PostgresqlDescriptorFacilities;
import com.extremum.jpa.facilities.StaticPostgresqlDescriptorFacilitiesAccessor;

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

        if (model.getId() == null && model.getUuid() != null) {
            model.setId(descriptorFacilities().resolve(model.getUuid()));
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
