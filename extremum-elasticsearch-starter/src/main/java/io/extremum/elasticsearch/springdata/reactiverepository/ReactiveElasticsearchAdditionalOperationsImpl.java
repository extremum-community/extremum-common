package io.extremum.elasticsearch.springdata.reactiverepository;

import io.extremum.elasticsearch.springdata.repository.Searcher;
import io.extremum.elasticsearch.springdata.repository.UpdatePreparation;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.search.SearchHit;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ResultsMapper;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactiveElasticsearchAdditionalOperationsImpl implements ReactiveElasticsearchAdditionalOperations {
    private final ReactiveElasticsearchClient client;
    private final ResultsMapper resultsMapper;

    private final UpdatePreparation updatePreparation;
    private final Searcher searcher;

    public ReactiveElasticsearchAdditionalOperationsImpl(ReactiveElasticsearchClient client,
                                                         ResultsMapper resultsMapper,
                                                         ElasticsearchOperations elasticsearchOperations) {
        this.client = client;
        this.resultsMapper = resultsMapper;

        updatePreparation = new UpdatePreparation(elasticsearchOperations);
        searcher = new Searcher(elasticsearchOperations);
    }

    @Override
    public Mono<UpdateResponse> update(UpdateQuery query) {
        UpdateRequest request = updatePreparation.prepareUpdate(query);
        return client.update(request);
    }

    @Override
    public <T> Flux<T> queryForPage(SearchQuery query, Class<T> clazz) {
        return queryForPage(query, clazz, resultsMapper);
    }

    private <T> Flux<T> queryForPage(SearchQuery query, Class<T> clazz, SearchResultMapper mapper) {
        Flux<SearchHit> hits = doSearch(searcher.prepareSearch(query, clazz), query);
        return hits.map(hit -> mapper.mapSearchHit(hit, clazz));
    }

    private Flux<SearchHit> doSearch(SearchRequest searchRequest, SearchQuery searchQuery) {
        searcher.prepareSearch(searchRequest, searchQuery);

        return client.search(searchRequest);
    }

}
