package io.extremum.elasticsearch.repositories;

import io.extremum.common.utils.ModelUtils;
import io.extremum.elasticsearch.facilities.ElasticsearchDescriptorFacilities;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.elasticsearch.action.index.IndexResponse;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author rpuch
 */
class SaveProcess {
    private final ElasticsearchDescriptorFacilities elasticsearchDescriptorFactory;

    private final SequenceNumberOperations sequenceNumberOperations = new SequenceNumberOperations();
    private final VersionOperations versionOperations = new VersionOperations();

    SaveProcess(ElasticsearchDescriptorFacilities elasticsearchDescriptorFactory) {
        this.elasticsearchDescriptorFactory = elasticsearchDescriptorFactory;
    }

    void prepareForSave(Object object) {
        ElasticsearchModels.asElasticsearchModel(object).ifPresent(model -> {
            fillIdFromDescriptor(model);
            fillIdIfStillMissing(model);
            createDescriptorIfNeeded(object);
            fillCreatedUpdated(model);
            fillDeleted(model);
        });
    }

    private void fillIdFromDescriptor(ElasticsearchCommonModel model) {
        if (model.getId() == null && model.getUuid() != null) {
            UUID resolvedId = elasticsearchDescriptorFactory.resolve(model.getUuid());
            model.setId(resolvedId.toString());
        }
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

    void fillAfterSave(Object object, IndexResponse response) {
        sequenceNumberOperations.setSequenceNumberAndPrimaryTermAfterIndexing(object, response);
        versionOperations.setVersionAfterIndexing(object, response);

        createDescriptorIfNeeded(object);
    }

    private void createDescriptorIfNeeded(Object object) {
        ElasticsearchModels.asElasticsearchModel(object).ifPresent(model -> {
            if (model.getUuid() == null) {
                String name = ModelUtils.getModelName(model);
                model.setUuid(elasticsearchDescriptorFactory.create(UUID.fromString(model.getId()), name));
            }
        });
    }
}
