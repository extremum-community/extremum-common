package io.extremum.elasticsearch.repositories;

import io.extremum.common.exceptions.ModelNotFoundException;
import io.extremum.common.models.PersistableCommonModel;
import io.extremum.common.utils.StreamUtils;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Differs from the standard {@link SimpleElasticsearchRepository} in two aspects:
 * 1. has implementations for our extension methods
 * 2. implements soft-deletion logic; that is, all deletions are replaced with setting 'deleted' flag to true,
 * and all find operations filter out documents with 'deleted' set to true.
 *
 * @author rpuch
 */
public class SoftDeleteElasticsearchRepository<T extends ElasticsearchCommonModel>
        extends BaseElasticsearchRepository<T> {
    private final ElasticsearchOperations elasticsearchOperations;

    private final SoftDeletion softDeletion = new SoftDeletion();

    public SoftDeleteElasticsearchRepository(
            ElasticsearchEntityInformation<T, String> metadata,
            ElasticsearchOperations elasticsearchOperations) {
        super(metadata, elasticsearchOperations);

        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public Iterable<T> search(QueryBuilder query) {
        QueryBuilder amendedQueryBuilder = softDeletion.amendQueryBuilderWithNotDeletedCondition(query);
        return super.search(amendedQueryBuilder);
    }

    @Override
    public Page<T> search(QueryBuilder query, Pageable pageable) {
        QueryBuilder amendedQueryBuilder = softDeletion.amendQueryBuilderWithNotDeletedCondition(query);
        return super.search(amendedQueryBuilder, pageable);
    }

    @Override
    public Page<T> search(SearchQuery query) {
        return super.search(new NonDeletedSearchQuery(query));
    }

    @Override
    public void deleteById(String id) {
        patch(id, "ctx._source.deleted = true");
    }

    @Override
    public T deleteByIdAndReturn(String id) {
        T model = findById(id).orElseThrow(() -> new ModelNotFoundException(entityClass, id));

        deleteById(id);

        // I did not find any way to do it 'honestly', so I'm applying a dirty patch. Actually, this is
        // deletion, and it seems unlikely that the exact deletion time be so important.
        model.setModified(ZonedDateTime.now());
        model.setDeleted(true);

        return model;
    }

    @Override
    public Optional<T> findById(String id) {
        return super.findById(id).filter(PersistableCommonModel::isNotDeleted);
    }

    @Override
    public Iterable<T> findAllById(Iterable<String> ids) {
        return StreamUtils.fromIterable(super.findAllById(ids))
                .filter(PersistableCommonModel::isNotDeleted)
                .collect(Collectors.toList());
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        CriteriaQuery query = new CriteriaQuery(softDeletion.notDeleted());
        query.setPageable(pageable);
        return elasticsearchOperations.queryForPage(query, getEntityClass());
    }

    @Override
    public long count() {
        return elasticsearchOperations.count(new CriteriaQuery(softDeletion.notDeleted()), getEntityClass());
    }
}
