package io.extremum.elasticsearch.springdata.reactiverepository;

import io.extremum.common.utils.ModelUtils;
import io.extremum.elasticsearch.facilities.ReactiveElasticsearchDescriptorFacilities;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import io.extremum.elasticsearch.springdata.repository.ReactiveElasticsearchModels;
import io.extremum.elasticsearch.springdata.repository.SequenceNumberOperations;
import io.extremum.elasticsearch.springdata.repository.VersionOperations;
import org.elasticsearch.action.index.IndexResponse;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author rpuch
 */
class ReactiveSaveProcess {
    private final ReactiveElasticsearchDescriptorFacilities elasticsearchDescriptorFactory;

    private final SequenceNumberOperations sequenceNumberOperations = new SequenceNumberOperations();
    private final VersionOperations versionOperations = new VersionOperations();

    ReactiveSaveProcess(ReactiveElasticsearchDescriptorFacilities elasticsearchDescriptorFactory) {
        this.elasticsearchDescriptorFactory = elasticsearchDescriptorFactory;
    }

    Mono<Void> prepareForSave(Object object) {
        return ReactiveElasticsearchModels.asElasticsearchModel(object)
                .flatMap(model -> {
                    return fillIdFromDescriptor(model)
                            .doOnSuccess(ignored -> fillIdIfStillMissing(model))
                            .then(createDescriptorIfNeeded(object))
                            .doOnSuccess(ignored -> fillCreatedUpdated(model))
                            .doOnSuccess(ignored -> fillDeleted(model));
                });
    }

    private Mono<Void> fillIdFromDescriptor(ElasticsearchCommonModel model) {
        if (model.getId() == null && model.getUuid() != null) {
            return elasticsearchDescriptorFactory.resolve(model.getUuid())
                    .map(Object::toString)
                    .doOnNext(model::setId)
                    .then();
        }

        return Mono.empty();
    }

    private void fillIdIfStillMissing(ElasticsearchCommonModel model) {
        if (model.getId() == null) {
            model.setId(newInternalId());
        }
    }

    private String newInternalId() {
        return UUID.randomUUID().toString();
    }

    private void fillCreatedUpdated(ElasticsearchCommonModel model) {
        if (model.getCreated() == null) {
            model.setCreated(ZonedDateTime.now());
        }
        if (model.getModified() == null) {
            model.setModified(ZonedDateTime.now());
        }
    }

    private void fillDeleted(ElasticsearchCommonModel model) {
        if (model.getDeleted() == null) {
            model.setDeleted(false);
        }
    }

    Mono<Void> fillAfterSave(Object object, IndexResponse response) {
        sequenceNumberOperations.setSequenceNumberAndPrimaryTermAfterIndexing(object, response);
        versionOperations.setVersionAfterIndexing(object, response);

        return createDescriptorIfNeeded(object);
    }

    private Mono<Void> createDescriptorIfNeeded(Object object) {
        return ReactiveElasticsearchModels.asElasticsearchModel(object)
                .flatMap(this::createAndFillDescriptorOnModelIfItIsNull);
    }

    private Mono<? extends Void> createAndFillDescriptorOnModelIfItIsNull(ElasticsearchCommonModel model) {
        if (model.getUuid() == null) {
            String name = ModelUtils.getModelName(model);
            return elasticsearchDescriptorFactory.create(UUID.fromString(model.getId()), name)
                    .doOnNext(model::setUuid)
                    .then();
        }
        return Mono.empty();
    }
}
