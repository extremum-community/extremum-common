package io.extremum.elasticsearch.springdata.repository;

import io.extremum.elasticsearch.model.ElasticsearchCommonModel;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @author rpuch
 */
public class ElasticsearchModels {
    public static Optional<ElasticsearchCommonModel> asElasticsearchModel(@Nullable Object object) {
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
