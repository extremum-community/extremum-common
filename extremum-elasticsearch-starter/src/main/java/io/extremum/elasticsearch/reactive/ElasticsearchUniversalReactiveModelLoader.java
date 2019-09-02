package io.extremum.elasticsearch.reactive;

import io.extremum.common.model.Model;
import io.extremum.common.model.PersistableCommonModel;
import io.extremum.common.support.UniversalReactiveModelLoader;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@RequiredArgsConstructor
public class ElasticsearchUniversalReactiveModelLoader implements UniversalReactiveModelLoader {
    private final ReactiveElasticsearchOperations reactiveElasticsearchOperations;

    @Override
    public Mono<Model> loadByInternalId(String internalId, Class<? extends Model> modelClass) {
        return reactiveElasticsearchOperations.findById(internalId, modelClass)
                .cast(ElasticsearchCommonModel.class)
                .filter(PersistableCommonModel::isNotDeleted)
                .map(Function.identity());
    }

    @Override
    public Descriptor.StorageType type() {
        return Descriptor.StorageType.ELASTICSEARCH;
    }
}
