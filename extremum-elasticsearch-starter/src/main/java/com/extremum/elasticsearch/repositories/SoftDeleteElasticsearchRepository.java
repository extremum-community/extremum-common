package com.extremum.elasticsearch.repositories;

import com.extremum.common.models.PersistableCommonModel;
import com.extremum.common.utils.DateUtils;
import com.extremum.common.utils.StreamUtils;
import com.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.facet.FacetRequest;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
public class SoftDeleteElasticsearchRepository<T extends ElasticsearchCommonModel> extends BaseElasticsearchRepository<T> {
    private static final String PAINLESS_LANGUAGE = "painless";

    private static final String MODIFIED = ElasticsearchCommonModel.FIELDS.modified.name();

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchEntityInformation<T, String> metadata;

    private final SoftDeletion softDeletion = new SoftDeletion();

    public SoftDeleteElasticsearchRepository(
            ElasticsearchEntityInformation<T, String> metadata,
            ElasticsearchOperations elasticsearchOperations) {
        super(metadata, elasticsearchOperations);

        this.elasticsearchOperations = elasticsearchOperations;
        this.metadata = metadata;
    }

    @Override
    public List<T> search(String queryString) {
        Iterable<T> results = search(QueryBuilders.queryStringQuery(queryString));
        return iterableToList(results);
    }

    @Override
    public Iterable<T> search(QueryBuilder query) {
        QueryBuilder amendedQueryBuilder = amendQueryBuilderWithNotDeletedCondition(query);
        return super.search(amendedQueryBuilder);
    }

    private QueryBuilder amendQueryBuilderWithNotDeletedCondition(QueryBuilder query) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        boolQueryBuilder.must(query);
        QueryBuilder notDeletedQuery = new CriteriaQueryProcessor().createQueryFromCriteria(softDeletion.notDeleted());
        boolQueryBuilder.must(notDeletedQuery);
        
        return boolQueryBuilder;
    }

    @Override
    public Page<T> search(QueryBuilder query, Pageable pageable) {
        QueryBuilder amendedQueryBuilder = amendQueryBuilderWithNotDeletedCondition(query);
        return super.search(amendedQueryBuilder, pageable);
    }

    @Override
    public Page<T> search(SearchQuery query) {
        return super.search(new NonDeletedSearchQuery(query));
    }

    @Override
    public boolean patch(String id, String painlessScript) {
        return patch(id, painlessScript, Collections.emptyMap());
    }

    @Override
    public boolean patch(String id, String painlessScript, Map<String, Object> scriptParams) {
        UpdateRequest updateRequest = new UpdateRequest(metadata.getIndexName(), id);
        Script script = createScript(painlessScript, scriptParams);
        updateRequest.script(script);

        UpdateQuery updateQuery = new UpdateQueryBuilder()
                .withClass(metadata.getJavaType())
                .withId(id)
                .withUpdateRequest(updateRequest)
                .build();

        UpdateResponse response = elasticsearchOperations.update(updateQuery);
        return response.getResult() == DocWriteResponse.Result.UPDATED;
    }

    private Script createScript(String painlessScript, Map<String, Object> params) {
        String scriptWithModificationTimeChange = amendWithModificationTimeChange(painlessScript);
        Map<String, Object> paramsWithModificationTimeChange = amendWithModificationTime(params);
        return new Script(ScriptType.INLINE, PAINLESS_LANGUAGE, scriptWithModificationTimeChange,
                paramsWithModificationTimeChange);
    }

    private String amendWithModificationTimeChange(String painlessScript) {
        return painlessScript + changeModificationTimeScriptSuffix();
    }

    private String changeModificationTimeScriptSuffix() {
        return "; ctx._source." + MODIFIED + " = params." + MODIFIED;
    }

    private Map<String, Object> amendWithModificationTime(Map<String, Object> params) {
        Map<String, Object> paramsWithModificationTimeChange = new HashMap<>(params);
        paramsWithModificationTimeChange.put(MODIFIED, getNowAsString());
        return paramsWithModificationTimeChange;
    }

    private String getNowAsString() {
        return DateUtils.formatZonedDateTimeISO_8601(ZonedDateTime.now());
    }

    @Override
    public void deleteById(String id) {
        patch(id, "ctx._source.deleted = true");
    }

    @Override
    public final void deleteAll() {
        throw new UnsupportedOperationException("We don't allow to delete all the documents in one go");
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

    private class NonDeletedSearchQuery implements SearchQuery {
        private final SearchQuery query;

        public NonDeletedSearchQuery(SearchQuery query) {
            this.query = query;
        }

        @Override
        public QueryBuilder getQuery() {
            return amendQueryBuilderWithNotDeletedCondition(query.getQuery());
        }

        @Override
        public QueryBuilder getFilter() {
            return query.getFilter();
        }

        @Override
        public List<SortBuilder> getElasticsearchSorts() {
            return query.getElasticsearchSorts();
        }

        @Override
        public List<FacetRequest> getFacets() {
            return query.getFacets();
        }

        @Override
        public List<AbstractAggregationBuilder> getAggregations() {
            return query.getAggregations();
        }

        @Override
        public HighlightBuilder getHighlightBuilder() {
            return query.getHighlightBuilder();
        }

        @Override
        public HighlightBuilder.Field[] getHighlightFields() {
            return query.getHighlightFields();
        }

        @Override
        public List<IndexBoost> getIndicesBoost() {
            return query.getIndicesBoost();
        }

        @Override
        public List<ScriptField> getScriptFields() {
            return query.getScriptFields();
        }

        @Override
        public <T extends Query> T setPageable(Pageable pageable) {
            return query.setPageable(pageable);
        }

        @Override
        public Pageable getPageable() {
            return query.getPageable();
        }

        @Override
        public <T extends Query> T addSort(Sort sort) {
            return query.addSort(sort);
        }

        @Override
        public Sort getSort() {
            return query.getSort();
        }

        @Override
        public List<String> getIndices() {
            return query.getIndices();
        }

        @Override
        public void addIndices(String... indices) {
            query.addIndices(indices);
        }

        @Override
        public void addTypes(String... types) {
            query.addTypes(types);
        }

        @Override
        public List<String> getTypes() {
            return query.getTypes();
        }

        @Override
        public void addFields(String... fields) {
            query.addFields(fields);
        }

        @Override
        public List<String> getFields() {
            return query.getFields();
        }

        @Override
        public void addSourceFilter(SourceFilter sourceFilter) {
            query.addSourceFilter(sourceFilter);
        }

        @Override
        public SourceFilter getSourceFilter() {
            return query.getSourceFilter();
        }

        @Override
        public float getMinScore() {
            return query.getMinScore();
        }

        @Override
        public boolean getTrackScores() {
            return query.getTrackScores();
        }

        @Override
        public Collection<String> getIds() {
            return query.getIds();
        }

        @Override
        public String getRoute() {
            return query.getRoute();
        }

        @Override
        public SearchType getSearchType() {
            return query.getSearchType();
        }

        @Override
        public IndicesOptions getIndicesOptions() {
            return query.getIndicesOptions();
        }
    }
}
