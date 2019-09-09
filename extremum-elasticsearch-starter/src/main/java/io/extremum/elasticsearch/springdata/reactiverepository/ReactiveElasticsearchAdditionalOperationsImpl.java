package io.extremum.elasticsearch.springdata.reactiverepository;

import io.extremum.elasticsearch.springdata.repository.UpdatePreparation;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import reactor.core.publisher.Mono;

public class ReactiveElasticsearchAdditionalOperationsImpl implements ReactiveElasticsearchAdditionalOperations {
    private final ReactiveElasticsearchClient client;

    private final UpdatePreparation updatePreparation;

    public ReactiveElasticsearchAdditionalOperationsImpl(ReactiveElasticsearchClient client,
                                                         ElasticsearchOperations elasticsearchOperations) {
        this.client = client;

        updatePreparation = new UpdatePreparation(elasticsearchOperations);
    }

    @Override
    public Mono<UpdateResponse> update(UpdateQuery query) {
        UpdateRequest request = updatePreparation.prepareUpdate(query);
        return client.update(request);
    }

}
