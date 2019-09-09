package io.extremum.elasticsearch.springdata.reactiverepository;

import org.elasticsearch.action.update.UpdateResponse;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import reactor.core.publisher.Mono;

public interface ReactiveElasticsearchAdditionalOperations {
    Mono<UpdateResponse> update(UpdateQuery updateQuery);
}
