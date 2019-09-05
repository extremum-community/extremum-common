package io.extremum.mongo.service.lifecycle;

import io.extremum.common.utils.ModelUtils;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
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
    private final ReactiveMongoDescriptorFacilities mongoDescriptorFacilities;

    public ReactiveMongoCommonModelLifecycleListener(ReactiveMongoDescriptorFacilities mongoDescriptorFacilities) {
        this.mongoDescriptorFacilities = mongoDescriptorFacilities;
    }

    @Override
    public Mono<Void> onBeforeConvert(BeforeConvertEvent<MongoCommonModel> event) {
        MongoCommonModel model = event.getSource();

        return fillRequiredFields(model);
    }
    
    private Mono<Void> fillRequiredFields(MongoCommonModel model) {
        final boolean internalIdGiven = model.getId() != null;
        final boolean uuidGiven = model.getUuid() != null;

        if (uuidGiven && !internalIdGiven) {
            return getInternalIdFromDescriptor(model)
                    .doOnNext(model::setId)
                    .then();
        } else if (!uuidGiven && internalIdGiven) {
            return createAndSaveDescriptorWithGivenInternalId(model.getId(), model)
                    .doOnNext(model::setUuid)
                    .then();
        } else if (!uuidGiven && !internalIdGiven) {
            return createAndSaveDescriptorWithGivenInternalId(newEntityId(), model)
                    .doOnNext(model::setUuid)
                    .then(getInternalIdFromDescriptor(model).doOnNext(model::setId))
                    .then();
        }

        return Mono.empty();
    }

    private Mono<ObjectId> getInternalIdFromDescriptor(MongoCommonModel model) {
        return Mono.just(model)
                .map(MongoCommonModel::getUuid)
                .flatMap(mongoDescriptorFacilities::resolve);
    }

    private Mono<Descriptor> createAndSaveDescriptorWithGivenInternalId(ObjectId objectId, MongoCommonModel model) {
        String modelName = ModelUtils.getModelName(model);
        return mongoDescriptorFacilities.create(objectId, modelName);
    }

    private ObjectId newEntityId() {
        return new ObjectId();
    }

    @Override
    public Mono<Void> onAfterSave(AfterSaveEvent<MongoCommonModel> event) {
        MongoCommonModel model = event.getSource();

        return createDescriptorIfNeeded(model);
    }

    private Mono<Void> createDescriptorIfNeeded(MongoCommonModel model) {
        if (model.getUuid() == null) {
            String name = ModelUtils.getModelName(model.getClass());
            return mongoDescriptorFacilities.create(model.getId(), name)
                    .doOnNext(model::setUuid)
                    .then();
        }

        return Mono.empty();
    }

    @Override
    public Mono<Void> onAfterConvert(AfterConvertEvent<MongoCommonModel> event) {
        MongoCommonModel model = event.getSource();

        return resolveDescriptor(model);
    }

    private Mono<Void> resolveDescriptor(MongoCommonModel model) {
        return mongoDescriptorFacilities.fromInternalId(model.getId())
                .doOnNext(model::setUuid)
                .then();
    }
}
