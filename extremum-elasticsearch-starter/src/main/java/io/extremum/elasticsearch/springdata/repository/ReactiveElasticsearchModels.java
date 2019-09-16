package io.extremum.elasticsearch.springdata.repository;

import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;

/**
 * @author rpuch
 */
public class ReactiveElasticsearchModels {
    public static Mono<ElasticsearchCommonModel> asElasticsearchModel(@Nullable Object object) {
        if (object == null) {
            return Mono.empty();
        }

        if (!(object instanceof ElasticsearchCommonModel)) {
            return Mono.empty();
        }

        ElasticsearchCommonModel model = (ElasticsearchCommonModel) object;
        return Mono.just(model);
    }
}
