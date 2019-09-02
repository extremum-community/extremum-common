package io.extremum.mongo.service.lifecycle;

import io.extremum.mongo.facilities.MongoDescriptorFacilities;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.common.utils.ModelUtils;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.bson.types.ObjectId;
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
        final boolean internalIdGiven = model.getId() != null;
        final boolean uuidGiven = model.getUuid() != null;

        if (uuidGiven && !internalIdGiven) {
            model.setId(getInternalIdFromDescriptor(model));
        } else if (!uuidGiven && internalIdGiven) {
            Descriptor descriptor = createAndSaveDescriptorWithGivenInternalId(model.getId(), model);
            model.setUuid(descriptor);
        } else if (!uuidGiven && !internalIdGiven) {
            Descriptor descriptor = createAndSaveDescriptorWithGivenInternalId(newEntityId(), model);
            model.setUuid(descriptor);
            model.setId(getInternalIdFromDescriptor(model));
        }
    }

    private ObjectId getInternalIdFromDescriptor(MongoCommonModel model) {
        return mongoDescriptorFacilities.resolve(model.getUuid());
    }

    private Descriptor createAndSaveDescriptorWithGivenInternalId(ObjectId objectId, MongoCommonModel model) {
        String modelName = ModelUtils.getModelName(model);
        return mongoDescriptorFacilities.create(objectId, modelName);
    }

    private ObjectId newEntityId() {
        return new ObjectId();
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
