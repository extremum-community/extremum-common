package com.extremum.common.service.lifecycle;

import com.extremum.common.descriptor.factory.MongoDescriptorFacilities;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.utils.ModelUtils;
import com.extremum.sharedmodels.descriptor.Descriptor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

/**
 * @author rpuch
 */
public final class MongoCommonModelLifecycleListener extends AbstractMongoEventListener<MongoCommonModel> {
    private final MongoDescriptorFacilities mongoDescriptorFacilities;

    public MongoCommonModelLifecycleListener(MongoDescriptorFacilities mongoDescriptorFacilities) {
        this.mongoDescriptorFacilities = mongoDescriptorFacilities;
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<MongoCommonModel> event) {
        super.onBeforeConvert(event);

        MongoCommonModel model = event.getSource();

        fillRequiredFields(model);
    }
    
    private void fillRequiredFields(MongoCommonModel model) {
        if (model.getId() == null && model.getUuid() != null) {
            model.setId(mongoDescriptorFacilities.resolve(model.getUuid()));
        } else if (model.getUuid() == null) {
            String modelName = ModelUtils.getModelName(model);
            Descriptor descriptor = mongoDescriptorFacilities.createWithNewInternalId(modelName);
            model.setUuid(descriptor);
            model.setId(mongoDescriptorFacilities.resolve(model.getUuid()));
        }
    }

    @Override
    public void onAfterSave(AfterSaveEvent<MongoCommonModel> event) {
        super.onAfterSave(event);
        
        MongoCommonModel model = event.getSource();

        createDescriptorIfNeeded(model);
    }

    private void createDescriptorIfNeeded(MongoCommonModel model) {
        String name = ModelUtils.getModelName(model.getClass());
        if (model.getUuid() == null) {
            model.setUuid(mongoDescriptorFacilities.create(model.getId(), name));
        }
    }

    @Override
    public void onAfterConvert(AfterConvertEvent<MongoCommonModel> event) {
        super.onAfterConvert(event);

        MongoCommonModel model = event.getSource();

        resolveDescriptor(model);
    }
    
    private void resolveDescriptor(MongoCommonModel model) {
        model.setUuid(mongoDescriptorFacilities.fromInternalId(model.getId()));
    }
}
