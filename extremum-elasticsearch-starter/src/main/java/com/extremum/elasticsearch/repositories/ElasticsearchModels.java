package com.extremum.elasticsearch.repositories;

import com.extremum.elasticsearch.model.ElasticsearchCommonModel;

import java.util.Optional;

/**
 * @author rpuch
 */
class ElasticsearchModels {
    static Optional<ElasticsearchCommonModel> asElasticsearchModel(Object object) {
        if (object == null) {
            return Optional.empty();
        }

        if (!(object instanceof ElasticsearchCommonModel)) {
            return Optional.empty();
        }

        ElasticsearchCommonModel model = (ElasticsearchCommonModel) object;
        return Optional.of(model);
    }
}
