package com.extremum.elasticsearch.repositories;

import com.extremum.common.models.PersistableCommonModel;
import com.extremum.common.utils.StreamUtils;
import com.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
public class SoftDeleteElasticsearchRepository<T extends ElasticsearchCommonModel> extends BaseElasticsearchRepository<T> {
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
