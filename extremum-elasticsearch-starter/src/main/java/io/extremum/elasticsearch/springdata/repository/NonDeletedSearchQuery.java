package io.extremum.elasticsearch.springdata.repository;

import io.extremum.elasticsearch.SoftDeletion;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.facet.FacetRequest;
import org.springframework.data.elasticsearch.core.query.*;

import java.util.Collection;
import java.util.List;

/**
 * @author rpuch
 */
class NonDeletedSearchQuery implements SearchQuery {
    private final SearchQuery query;

    public NonDeletedSearchQuery(SearchQuery query) {
        this.query = query;
    }

    @Override
    public QueryBuilder getQuery() {
        return new SoftDeletion().amendQueryBuilderWithNotDeletedCondition(query.getQuery());
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
