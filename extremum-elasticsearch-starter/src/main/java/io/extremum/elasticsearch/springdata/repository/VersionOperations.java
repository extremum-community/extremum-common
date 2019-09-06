package io.extremum.elasticsearch.springdata.repository;

import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.elasticsearch.action.index.IndexResponse;

/**
 * @author rpuch
 */
public class VersionOperations {
    public void setVersionAfterIndexing(Object indexedEntity, IndexResponse response) {
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
