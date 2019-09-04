package io.extremum.mongo.service.lifecycle;

import io.extremum.common.utils.ModelUtils;
import io.extremum.mongo.facilities.MongoDescriptorFacilities;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.mongo.springdata.lifecycle.AbstractReactiveMongoEventListener;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public final class ReactiveMongoCommonModelLifecycleListener
        extends AbstractReactiveMongoEventListener<MongoCommonModel> {
    private final MongoDescriptorFacilities mongoDescriptorFacilities;

    public ReactiveMongoCommonModelLifecycleListener(MongoDescriptorFacilities mongoDescriptorFacilities) {
        this.mongoDescriptorFacilities = mongoDescriptorFacilities;
    }

    @Override
    public Mono<Void> onBeforeConvert(BeforeConvertEvent<MongoCommonModel> event) {
        MongoCommonModel model = event.getSource();

        fillRequiredFields(model);

        return Mono.empty();
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
    public Mono<Void> onAfterSave(AfterSaveEvent<MongoCommonModel> event) {
        MongoCommonModel model = event.getSource();

        createDescriptorIfNeeded(model);

        return Mono.empty();
    }

    private void createDescriptorIfNeeded(MongoCommonModel model) {
        if (model.getUuid() == null) {
            String name = ModelUtils.getModelName(model.getClass());
            model.setUuid(mongoDescriptorFacilities.create(model.getId(), name));
        }
    }

    @Override
    public Mono<Void> onAfterConvert(AfterConvertEvent<MongoCommonModel> event) {
        MongoCommonModel model = event.getSource();

        resolveDescriptor(model);

        return Mono.empty();
    }

    private void resolveDescriptor(MongoCommonModel model) {
        model.setUuid(mongoDescriptorFacilities.fromInternalId(model.getId()));
    }
}
