package com.extremum.elasticsearch.repositories;

import com.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.elasticsearch.action.index.IndexResponse;

/**
 * @author rpuch
 */
class VersionOperations {
    void setVersionAfterIndexing(Object indexedEntity, IndexResponse response) {
        if (indexedEntity == null) {
            return;
        }
        if (!(indexedEntity instanceof ElasticsearchCommonModel)) {
            return;
        }

        ElasticsearchCommonModel model = (ElasticsearchCommonModel) indexedEntity;
        model.setVersion(response.getVersion());
    }
}
