package com.extremum.elasticsearch.repositories;

import com.extremum.common.utils.ModelUtils;
import com.extremum.elasticsearch.factory.ElasticsearchDescriptorFactory;
import com.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.elasticsearch.action.index.IndexResponse;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author rpuch
 */
class SaveProcess {
    private final ElasticsearchDescriptorFactory elasticsearchDescriptorFactory;

    private final SequenceNumberOperations sequenceNumberOperations = new SequenceNumberOperations();
    private final VersionOperations versionOperations = new VersionOperations();

    SaveProcess(ElasticsearchDescriptorFactory elasticsearchDescriptorFactory) {
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
            model.setId(UUID.randomUUID().toString());
        }
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
