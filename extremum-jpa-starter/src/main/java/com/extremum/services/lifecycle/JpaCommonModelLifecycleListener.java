package com.extremum.services.lifecycle;

import com.extremum.common.models.BasicModel;
import com.extremum.common.utils.ModelUtils;
import com.extremum.factory.PostgresqlDescriptorFactory;
import com.extremum.factory.StaticPostgresqlDescriptorFactoryAccessor;

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
        ensureFactoryIsAvailable();

        if (model.getId() == null && model.getUuid() != null) {
            model.setId(descriptorFactory().resolve(model.getUuid()));
        }
    }

    private void ensureFactoryIsAvailable() {
        if (descriptorFactory() == null) {
            throw new IllegalStateException("PostgresqlDescriptorFactory is not available");
        }
    }

    private PostgresqlDescriptorFactory descriptorFactory() {
        return StaticPostgresqlDescriptorFactoryAccessor.getFactory();
    }

    @PostPersist
    public void createDescriptorIfNeeded(BasicModel<UUID> model) {
        ensureFactoryIsAvailable();

        if (model.getUuid() == null) {
            model.setUuid(descriptorFactory().create(model.getId(), ModelUtils.getModelName(model)));
        }
    }

    @PostLoad
    public void onAfterConvert(BasicModel<UUID> model) {
        ensureFactoryIsAvailable();

        model.setUuid(descriptorFactory().fromInternalId(model.getId()));
    }
}
