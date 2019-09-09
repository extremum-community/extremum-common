package io.extremum.elasticsearch.springdata.reactiverepository;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.model.PersistableCommonModel;
import io.extremum.elasticsearch.SoftDeletion;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.data.elasticsearch.repository.support.SimpleReactiveElasticsearchRepository;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;

/**
 * Differs from the standard {@link SimpleReactiveElasticsearchRepository} in two aspects:
 * 1. has implementations for our extension methods
 * 2. implements soft-deletion logic; that is, all deletions are replaced with setting 'deleted' flag to true,
 * and all find operations filter out documents with 'deleted' set to true.
 *
 * @author rpuch
 */
public class SoftDeleteReactiveElasticsearchRepository<T extends ElasticsearchCommonModel>
        extends BaseReactiveElasticsearchRepository<T> {
    private final ElasticsearchEntityInformation<T, String> metadata;
    private final ReactiveElasticsearchOperations elasticsearchOperations;

    private final SoftDeletion softDeletion = new SoftDeletion();

    public SoftDeleteReactiveElasticsearchRepository(
            ElasticsearchEntityInformation<T, String> metadata,
            ReactiveElasticsearchOperations elasticsearchOperations) {
        super(metadata, elasticsearchOperations);

        this.metadata = metadata;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public Flux<T> search(QueryBuilder query) {
        QueryBuilder amendedQueryBuilder = softDeletion.amendQueryBuilderWithNotDeletedCondition(query);
        return super.search(amendedQueryBuilder);
    }

    @Override
    public Mono<Void> delete(T entity) {
        return deleteById(entity.getId());
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return patch(id, "ctx._source.deleted = true")
                .then();
    }

    @Override
    public Mono<T> deleteByIdAndReturn(String id) {
        return findById(id)
                .switchIfEmpty(Mono.error(new ModelNotFoundException(metadata.getJavaType(), id)))
                .flatMap(model -> deleteById(id).thenReturn(model))
                .doOnNext(model -> {
                    // I did not find any way to do it 'honestly', so I'm applying a dirty patch.
                    // Actually, this is deletion, and it seems unlikely that the exact deletion time
                    // be so important.
                    model.setModified(ZonedDateTime.now());
                    model.setDeleted(true);
                });
    }

    @Override
    public Mono<T> findById(String id) {
        return super.findById(id).filter(PersistableCommonModel::isNotDeleted);
    }

    @Override
    public Flux<T> findAllById(Iterable<String> ids) {
        return super.findAllById(ids)
                .filter(PersistableCommonModel::isNotDeleted);
    }

    @Override
    public Mono<Long> count() {
        return elasticsearchOperations.count(new CriteriaQuery(softDeletion.notDeleted()), metadata.getJavaType());
    }

    @Override
    public Mono<Boolean> existsById(String id) {
        Assert.notNull(id, "Id must not be null!");

        return findById(id)
                .map(model -> true)
                .switchIfEmpty(Mono.just(false));
    }
}
