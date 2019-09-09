package io.extremum.elasticsearch.springdata.repository;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.facet.FacetRequest;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

import static org.springframework.util.CollectionUtils.isEmpty;

public class SearchPreparation {
    private final ElasticsearchOperations elasticsearchOperations;

    public SearchPreparation(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public <T> SearchRequest prepareSearch(Query query, Class<T> clazz) {
        setPersistentEntityIndexAndType(query, clazz);
        return prepareSearch(query, Optional.empty());
    }

    public  <T> SearchRequest prepareSearch(SearchQuery query, Class<T> clazz) {
        setPersistentEntityIndexAndType(query, clazz);
        return prepareSearch(query, Optional.ofNullable(query.getQuery()));
    }

    private void setPersistentEntityIndexAndType(Query query, Class clazz) {
        if (query.getIndices().isEmpty()) {
            query.addIndices(retrieveIndexNameFromPersistentEntity(clazz));
        }
        if (query.getTypes().isEmpty()) {
            query.addTypes(retrieveTypeFromPersistentEntity(clazz));
        }
    }

    private String[] retrieveIndexNameFromPersistentEntity(Class clazz) {
        if (clazz != null) {
            return new String[]{elasticsearchOperations.getPersistentEntityFor(clazz).getIndexName()};
        }
        return null;
    }

    private String[] retrieveTypeFromPersistentEntity(Class clazz) {
        if (clazz != null) {
            return new String[]{elasticsearchOperations.getPersistentEntityFor(clazz).getIndexType()};
        }
        return null;
    }

    private SearchRequest prepareSearch(Query query, Optional<QueryBuilder> builder) {
        Assert.notNull(query.getIndices(), "No index defined for Query");
        Assert.notNull(query.getTypes(), "No type defined for Query");

        int startRecord = 0;
        SearchRequest request = new SearchRequest(toArray(query.getIndices()));
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        request.types(toArray(query.getTypes()));
        sourceBuilder.version(true);
        sourceBuilder.trackScores(query.getTrackScores());

        if (builder.isPresent()) {
            sourceBuilder.query(builder.get());
        }

        if (query.getSourceFilter() != null) {
            SourceFilter sourceFilter = query.getSourceFilter();
            sourceBuilder.fetchSource(sourceFilter.getIncludes(), sourceFilter.getExcludes());
        }

        if (query.getPageable().isPaged()) {
            startRecord = query.getPageable().getPageNumber() * query.getPageable().getPageSize();
            sourceBuilder.size(query.getPageable().getPageSize());
        }
        sourceBuilder.from(startRecord);

        if (!query.getFields().isEmpty()) {
            sourceBuilder.fetchSource(toArray(query.getFields()), null);
        }

        if (query.getIndicesOptions() != null) {
            request.indicesOptions(query.getIndicesOptions());
        }

        if (query.getSort() != null) {
            prepareSort(query, sourceBuilder);
        }

        if (query.getMinScore() > 0) {
            sourceBuilder.minScore(query.getMinScore());
        }

        // extremum addition: request that Elasticsearch return seq_no and primary_term for each search hit
        sourceBuilder.seqNoAndPrimaryTerm(true);

        request.source(sourceBuilder);
        return request;
    }

    private static String[] toArray(List<String> values) {
        String[] valuesAsArray = new String[values.size()];
        return values.toArray(valuesAsArray);
    }

    private void prepareSort(Query query, SearchSourceBuilder sourceBuilder) {
        for (Sort.Order order : query.getSort()) {
            FieldSortBuilder sort = SortBuilders.fieldSort(order.getProperty())
                    .order(order.getDirection().isDescending() ? SortOrder.DESC : SortOrder.ASC);
            if (order.getNullHandling() == Sort.NullHandling.NULLS_FIRST) {
                sort.missing("_first");
            } else if (order.getNullHandling() == Sort.NullHandling.NULLS_LAST) {
                sort.missing("_last");
            }
            sourceBuilder.sort(sort);
        }
    }

    public SearchRequest prepareSearch(SearchRequest searchRequest, SearchQuery searchQuery) {
        if (searchQuery.getFilter() != null) {
            searchRequest.source().postFilter(searchQuery.getFilter());
        }

        if (!isEmpty(searchQuery.getElasticsearchSorts())) {
            for (SortBuilder sort : searchQuery.getElasticsearchSorts()) {
                searchRequest.source().sort(sort);
            }
        }

        if (!searchQuery.getScriptFields().isEmpty()) {
            // _source should be return all the time
            // searchRequest.addStoredField("_source");
            for (ScriptField scriptedField : searchQuery.getScriptFields()) {
                searchRequest.source().scriptField(scriptedField.fieldName(), scriptedField.script());
            }
        }

        if (searchQuery.getHighlightFields() != null || searchQuery.getHighlightBuilder() != null) {
            HighlightBuilder highlightBuilder = searchQuery.getHighlightBuilder();
            if (highlightBuilder == null) {
                highlightBuilder = new HighlightBuilder();
            }
            if (searchQuery.getHighlightFields() != null) {
                for (HighlightBuilder.Field highlightField : searchQuery.getHighlightFields()) {
                    highlightBuilder.field(highlightField);
                }
            }
            searchRequest.source().highlighter(highlightBuilder);
        }

        if (!isEmpty(searchQuery.getIndicesBoost())) {
            for (IndexBoost indexBoost : searchQuery.getIndicesBoost()) {
                searchRequest.source().indexBoost(indexBoost.getIndexName(), indexBoost.getBoost());
            }
        }

        if (!isEmpty(searchQuery.getAggregations())) {
            for (AbstractAggregationBuilder aggregationBuilder : searchQuery.getAggregations()) {
                searchRequest.source().aggregation(aggregationBuilder);
            }
        }

        if (!isEmpty(searchQuery.getFacets())) {
            for (FacetRequest aggregatedFacet : searchQuery.getFacets()) {
                searchRequest.source().aggregation(aggregatedFacet.getFacet());
            }
        }
        return searchRequest;
    }
}
