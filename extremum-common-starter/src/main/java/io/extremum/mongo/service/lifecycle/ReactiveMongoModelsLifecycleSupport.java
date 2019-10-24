package io.extremum.mongo.service.lifecycle;

import io.extremum.common.model.PersistableCommonModel;
import io.extremum.common.utils.ModelUtils;
import io.extremum.mongo.facilities.ReactiveMongoDescriptorFacilities;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.bson.types.ObjectId;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public final class ReactiveMongoModelsLifecycleSupport {
    private final ReactiveMongoDescriptorFacilities mongoDescriptorFacilities;

    public ReactiveMongoModelsLifecycleSupport(ReactiveMongoDescriptorFacilities mongoDescriptorFacilities) {
        this.mongoDescriptorFacilities = mongoDescriptorFacilities;
    }

    Mono<Void> fillRequiredFields(PersistableCommonModel<ObjectId> model) {
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

    private Mono<ObjectId> getInternalIdFromDescriptor(PersistableCommonModel<ObjectId> model) {
        return Mono.just(model)
                .map(PersistableCommonModel::getUuid)
                .flatMap(mongoDescriptorFacilities::resolve);
    }

    private Mono<Descriptor> createAndSaveDescriptorWithGivenInternalId(ObjectId objectId,
                                                                        PersistableCommonModel<ObjectId> model) {
        String modelName = ModelUtils.getModelName(model);
        return mongoDescriptorFacilities.create(objectId, modelName);
    }

    private ObjectId newEntityId() {
        return new ObjectId();
    }

    Mono<Void> createDescriptorIfNeeded(PersistableCommonModel<ObjectId> model) {
        if (model.getUuid() == null) {
            String name = ModelUtils.getModelName(model.getClass());
            return mongoDescriptorFacilities.create(model.getId(), name)
                    .doOnNext(model::setUuid)
                    .then();
        }

        return Mono.empty();
    }

    Mono<Void> resolveDescriptor(PersistableCommonModel<ObjectId> model) {
        return mongoDescriptorFacilities.fromInternalId(model.getId())
                .doOnNext(model::setUuid)
                .then();
    }
}
