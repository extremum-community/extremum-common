package io.extremum.elasticsearch.springdata.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.elasticsearch.facilities.ElasticsearchDescriptorFacilities;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.ElasticsearchException;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.GetResultMapper;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.client.Requests.refreshRequest;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * @author rpuch
 */
public class ExtremumElasticsearchRestTemplate extends ElasticsearchRestTemplate {
    private static final Logger logger = LoggerFactory.getLogger(ExtremumElasticsearchRestTemplate.class);

    private final SequenceNumberOperations sequenceNumberOperations = new SequenceNumberOperations();
    private final SaveProcess saveProcess;
    private final Searcher searcher;

    public ExtremumElasticsearchRestTemplate(RestHighLevelClient client,
            ObjectMapper objectMapper,
            ElasticsearchDescriptorFacilities descriptorFacilities) {
        super(client,
                new ExtremumResultMapper(
                        new ExtremumEntityMapper(new SimpleElasticsearchMappingContext(), objectMapper)
                )
        );

        saveProcess = new SaveProcess(descriptorFacilities);
        searcher = new Searcher(this);
    }

    private static String[] toArray(List<String> values) {
        String[] valuesAsArray = new String[values.size()];
        return values.toArray(valuesAsArray);
    }

    @Override
    public String index(IndexQuery query) {
        if (query.getObject() != null) {
            saveProcess.prepareForSave(query.getObject());
        }

        String documentId;
        IndexRequest request = prepareIndex(query);
        IndexResponse response;
        try {
            response = getClient().index(request, RequestOptions.DEFAULT);
            documentId = response.getId();
        } catch (IOException e) {
            throw new ElasticsearchException("Error while index for request: " + request.toString(), e);
        }
        // We should call this because we are not going through a mapper.
        if (query.getObject() != null) {
            setPersistentEntityId(query.getObject(), documentId);
            saveProcess.fillAfterSave(query.getObject(), response);
        }
        return documentId;
    }

    private IndexRequest prepareIndex(IndexQuery query) {
        try {
            String indexName = StringUtils.isEmpty(query.getIndexName())
                    ? retrieveIndexNameFromPersistentEntity(query.getObject().getClass())[0]
                    : query.getIndexName();
            String type = StringUtils.isEmpty(query.getType())
                    ? retrieveTypeFromPersistentEntity(query.getObject().getClass())[0]
                    : query.getType();

            IndexRequest indexRequest;

            if (query.getObject() != null) {
                String id = StringUtils.isEmpty(query.getId()) ? getPersistentEntityId(
                        query.getObject()) : query.getId();
                // If we have a query id and a document id, do not ask ES to generate one.
                if (id != null) {
                    indexRequest = new IndexRequest(indexName, type, id);
                } else {
                    indexRequest = new IndexRequest(indexName, type);
                }
                indexRequest.source(getResultsMapper().getEntityMapper().mapToString(query.getObject()),
                        Requests.INDEX_CONTENT_TYPE);
            } else if (query.getSource() != null) {
                indexRequest = new IndexRequest(indexName, type, query.getId()).source(query.getSource(),
                        Requests.INDEX_CONTENT_TYPE);
            } else {
                throw new ElasticsearchException(
                        "object or source is null, failed to index the document [id: " + query.getId() + "]");
            }
            if (query.getVersion() != null) {
                indexRequest.version(query.getVersion());
                VersionType versionType = retrieveVersionTypeFromPersistentEntity(query.getObject().getClass());
                indexRequest.versionType(versionType);
            }

            // in elasticsearch 7.1.0 client library, there is no parent() method
//            if (query.getParentId() != null) {
//                indexRequest.parent(query.getParentId());
//            }

            // This is the only thing we needed to add to this method for extremum-specific tasks
            sequenceNumberOperations.fillSequenceNumberAndPrimaryTermOnIndexRequest(query.getObject(), indexRequest);

            return indexRequest;
        } catch (IOException e) {
            throw new ElasticsearchException("failed to index the document [id: " + query.getId() + "]", e);
        }
    }

    private String[] retrieveIndexNameFromPersistentEntity(Class clazz) {
        if (clazz != null) {
            return new String[]{getPersistentEntityFor(clazz).getIndexName()};
        }
        return null;
    }

    private void setPersistentEntityId(Object entity, String id) {

        ElasticsearchPersistentEntity<?> persistentEntity = getPersistentEntityFor(entity.getClass());
        ElasticsearchPersistentProperty idProperty = persistentEntity.getIdProperty();

        // Only deal with text because ES generated Ids are strings !

        if (idProperty != null && idProperty.getType().isAssignableFrom(String.class)) {
            persistentEntity.getPropertyAccessor(entity).setProperty(idProperty, id);
        }
    }

    private String[] retrieveTypeFromPersistentEntity(Class clazz) {
        if (clazz != null) {
            return new String[]{getPersistentEntityFor(clazz).getIndexType()};
        }
        return null;
    }

    private VersionType retrieveVersionTypeFromPersistentEntity(Class clazz) {
        if (clazz != null) {
            return getPersistentEntityFor(clazz).getVersionType();
        }
        return VersionType.EXTERNAL;
    }

    private String getPersistentEntityId(Object entity) {

        ElasticsearchPersistentEntity<?> persistentEntity = getPersistentEntityFor(entity.getClass());
        Object identifier = persistentEntity.getIdentifierAccessor(entity).getIdentifier();

        if (identifier != null) {
            return identifier.toString();
        }

        return null;
    }

    @Override
    public void bulkIndex(List<IndexQuery> queries) {
        prepareBulkForSave(queries);

        BulkRequest bulkRequest = new BulkRequest();
        for (IndexQuery query : queries) {
            bulkRequest.add(prepareIndex(query));
        }

        BulkResponse bulkResponse;
        try {
            bulkResponse = getClient().bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ElasticsearchException("Error while bulk for request: " + bulkRequest.toString(), e);
        }
        checkForBulkUpdateFailure(bulkResponse);

        if (bulkResponse.getItems().length != queries.size()) {
            String message = String.format("There were %d queries but %d responses in bulk",
                    queries.size(), bulkResponse.getItems().length);
            throw new IllegalStateException(message);
        }

        fillAfterBulkSave(queries, bulkResponse);
    }

    private void prepareBulkForSave(List<IndexQuery> queries) {
        for (IndexQuery query : queries) {
            if (query.getObject() != null) {
                saveProcess.prepareForSave(query.getObject());
            }
        }
    }

    private void fillAfterBulkSave(List<IndexQuery> queries, BulkResponse bulkResponse) {
        for (int i = 0; i < queries.size(); i++) {
            IndexQuery query = queries.get(i);
            BulkItemResponse responseItem = bulkResponse.getItems()[i];
            IndexResponse indexResponse = responseItem.getResponse();
            if (query.getObject() != null) {
                saveProcess.fillAfterSave(query.getObject(), indexResponse);
            }
        }
    }

    private void checkForBulkUpdateFailure(BulkResponse bulkResponse) {
        if (bulkResponse.hasFailures()) {
            Map<String, String> failedDocuments = new HashMap<>();
            for (BulkItemResponse item : bulkResponse.getItems()) {
                if (item.isFailed()) {
                    failedDocuments.put(item.getId(), item.getFailureMessage());
                }
            }
            throw new ElasticsearchException(
                    "Bulk indexing has failures. Use ElasticsearchException.getFailedDocuments() for detailed messages ["
                            + failedDocuments + "]",
                    failedDocuments);
        }
    }

    // Code that follows is needed just to work-around the fact that Spring Data Elasticsearch 3.2 uses old
    // request method signatures which were removed in 7.1.0 (or earlier). Hence the copy-paste.

    @Override
    public boolean indexExists(String indexName) {
        GetIndexRequest request = new GetIndexRequest();
        request.indices(indexName);
        try {
            return getClient().indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ElasticsearchException("Error while for indexExists request: " + request.toString(), e);
        }
    }

    @Override
    public boolean putMapping(String indexName, String type, Object mapping) {
        Assert.notNull(indexName, "No index defined for putMapping()");
        Assert.notNull(type, "No type defined for putMapping()");
        PutMappingRequest request = new PutMappingRequest(indexName).type(type);
        if (mapping instanceof String) {
            request.source(String.valueOf(mapping), XContentType.JSON);
        } else if (mapping instanceof Map) {
            request.source((Map) mapping);
        } else if (mapping instanceof XContentBuilder) {
            request.source((XContentBuilder) mapping);
        }
        try {
            return getClient().indices().putMapping(request, RequestOptions.DEFAULT).isAcknowledged();
        } catch (IOException e) {
            throw new ElasticsearchException("Failed to put mapping for " + indexName, e);
        }
    }

    @Override
    public void refresh(String indexName) {
        Assert.notNull(indexName, "No index defined for refresh()");
        try {
            // TODO: Do something with the response.
            getClient().indices().refresh(refreshRequest(indexName), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ElasticsearchException("failed to refresh index: " + indexName, e);
        }
    }

    @Override
    public <T> long count(SearchQuery searchQuery, Class<T> clazz) {
        QueryBuilder elasticsearchQuery = searchQuery.getQuery();
        QueryBuilder elasticsearchFilter = searchQuery.getFilter();

        if (elasticsearchFilter == null) {
            return doCount(prepareCount(searchQuery, clazz), elasticsearchQuery);
        } else {
            // filter could not be set into CountRequestBuilder, convert request into search request
            return doCount(searcher.prepareSearch(searchQuery, clazz), elasticsearchQuery, elasticsearchFilter);
        }
    }

    private long doCount(SearchRequest countRequest, QueryBuilder elasticsearchQuery) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        if (elasticsearchQuery != null) {
            sourceBuilder.query(elasticsearchQuery);
        }
        countRequest.source(sourceBuilder);

        try {
            return getClient().search(countRequest, RequestOptions.DEFAULT).getHits().getTotalHits().value;
        } catch (IOException e) {
            throw new ElasticsearchException("Error while searching for request: " + countRequest.toString(), e);
        }
    }

    private <T> SearchRequest prepareCount(Query query, Class<T> clazz) {
        String indexName[] = !isEmpty(query.getIndices())
                ? query.getIndices().toArray(new String[query.getIndices().size()])
                : retrieveIndexNameFromPersistentEntity(clazz);
        String types[] = !isEmpty(query.getTypes()) ? query.getTypes().toArray(new String[query.getTypes().size()])
                : retrieveTypeFromPersistentEntity(clazz);

        Assert.notNull(indexName, "No index defined for Query");

        SearchRequest countRequestBuilder = new SearchRequest(indexName);

        if (types != null) {
            countRequestBuilder.types(types);
        }
        return countRequestBuilder;
    }

    private long doCount(SearchRequest searchRequest, QueryBuilder elasticsearchQuery,
            QueryBuilder elasticsearchFilter) {
        if (elasticsearchQuery != null) {
            searchRequest.source().query(elasticsearchQuery);
        } else {
            searchRequest.source().query(QueryBuilders.matchAllQuery());
        }
        if (elasticsearchFilter != null) {
            searchRequest.source().postFilter(elasticsearchFilter);
        }
        SearchResponse response;
        try {
            response = getClient().search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ElasticsearchException("Error for search request: " + searchRequest.toString(), e);
        }
        return response.getHits().getTotalHits().value;
    }

    @Override
    public <T> long count(CriteriaQuery criteriaQuery, Class<T> clazz) {
        QueryBuilder elasticsearchQuery = new CriteriaQueryProcessor().createQueryFromCriteria(
                criteriaQuery.getCriteria());
        QueryBuilder elasticsearchFilter = new CriteriaFilterProcessor()
                .createFilterFromCriteria(criteriaQuery.getCriteria());

        if (elasticsearchFilter == null) {
            return doCount(prepareCount(criteriaQuery, clazz), elasticsearchQuery);
        } else {
            // filter could not be set into CountRequestBuilder, convert request into search request
            return doCount(searcher.prepareSearch(criteriaQuery, clazz), elasticsearchQuery, elasticsearchFilter);
        }
    }

    @Override
    public <T> AggregatedPage<T> queryForPage(SearchQuery query, Class<T> clazz, SearchResultMapper mapper) {
        SearchResponse response = doSearch(searcher.prepareSearch(query, clazz), query);
        return mapper.mapResults(response, clazz, query.getPageable());
    }

    @Override
    public <T> Page<T> queryForPage(CriteriaQuery criteriaQuery, Class<T> clazz) {
        QueryBuilder elasticsearchQuery = new CriteriaQueryProcessor().createQueryFromCriteria(
                criteriaQuery.getCriteria());
        QueryBuilder elasticsearchFilter = new CriteriaFilterProcessor()
                .createFilterFromCriteria(criteriaQuery.getCriteria());
        SearchRequest request = searcher.prepareSearch(criteriaQuery, clazz);

        if (elasticsearchQuery != null) {
            request.source().query(elasticsearchQuery);
        } else {
            request.source().query(QueryBuilders.matchAllQuery());
        }

        if (criteriaQuery.getMinScore() > 0) {
            request.source().minScore(criteriaQuery.getMinScore());
        }

        if (elasticsearchFilter != null) {
            request.source().postFilter(elasticsearchFilter);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("doSearch query:\n" + request.toString());
        }

        SearchResponse response;
        try {
            response = getClient().search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ElasticsearchException("Error for search request: " + request.toString(), e);
        }
        return getResultsMapper().mapResults(response, clazz, criteriaQuery.getPageable());
    }

    private SearchResponse doSearch(SearchRequest searchRequest, SearchQuery searchQuery) {
        searcher.prepareSearch(searchRequest, searchQuery);

        try {
            return getClient().search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ElasticsearchException("Error for search request with scroll: " + searchRequest.toString(), e);
        }
    }

    @Override
    public <T> T queryForObject(GetQuery query, Class<T> clazz, GetResultMapper mapper) {
        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);
        GetRequest request = new GetRequest(persistentEntity.getIndexName(), persistentEntity.getIndexType(),
                query.getId());
        GetResponse response;
        try {
            response = getClient().get(request, RequestOptions.DEFAULT);
            T entity = mapper.mapResult(response, clazz);
            return entity;
        } catch (IOException e) {
            throw new ElasticsearchException("Error while getting for request: " + request.toString(), e);
        }
    }

    @Override
    public String delete(String indexName, String type, String id) {
        DeleteRequest request = new DeleteRequest(indexName, type, id);
        try {
            return getClient().delete(request, RequestOptions.DEFAULT).getId();
        } catch (IOException e) {
            throw new ElasticsearchException("Error while deleting item request: " + request.toString(), e);
        }
    }

    @Override
    public boolean createIndex(String indexName, Object settings) {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        if (settings instanceof String) {
            request.settings(String.valueOf(settings), Requests.INDEX_CONTENT_TYPE);
        } else if (settings instanceof Map) {
            request.settings((Map) settings);
        } else if (settings instanceof XContentBuilder) {
            request.settings((XContentBuilder) settings);
        }
        try {
            return getClient().indices().create(request, RequestOptions.DEFAULT).isAcknowledged();
        } catch (IOException e) {
            throw new ElasticsearchException("Error for creating index: " + request.toString(), e);
        }
    }

    @Override
    public <T> List<T> multiGet(SearchQuery searchQuery, Class<T> clazz) {
        return getResultsMapper().mapResults(getMultiResponse(searchQuery, clazz), clazz);
    }

    private <T> MultiGetResponse getMultiResponse(Query searchQuery, Class<T> clazz) {

        String indexName = !isEmpty(searchQuery.getIndices()) ? searchQuery.getIndices().get(0)
                : getPersistentEntityFor(clazz).getIndexName();
        String type = !isEmpty(searchQuery.getTypes()) ? searchQuery.getTypes().get(0)
                : getPersistentEntityFor(clazz).getIndexType();

        Assert.notNull(indexName, "No index defined for Query");
        Assert.notNull(type, "No type define for Query");
        Assert.notEmpty(searchQuery.getIds(), "No Id define for Query");

        MultiGetRequest request = new MultiGetRequest();

        if (searchQuery.getFields() != null && !searchQuery.getFields().isEmpty()) {
            searchQuery.addSourceFilter(new FetchSourceFilter(toArray(searchQuery.getFields()), null));
        }

        for (String id : searchQuery.getIds()) {

            MultiGetRequest.Item item = new MultiGetRequest.Item(indexName, type, id);

            if (searchQuery.getRoute() != null) {
                item = item.routing(searchQuery.getRoute());
            }

            request.add(item);
        }
        try {
            return getClient().multiGet(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ElasticsearchException("Error while multiget for request: " + request.toString(), e);
        }
    }

    @Override
    public UpdateResponse update(UpdateQuery query) {
        UpdateRequest request = new UpdatePreparation(this).prepareUpdate(query);
        try {
            return getClient().update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ElasticsearchException("Error while update for request: " + request.toString(), e);
        }
    }

    // Here ends the copy-paste due to the fact that Spring Data Elasticsearch 3.2 uses old
    // request method signatures which were removed in Elasticsearch 7.1.0 (or earlier)
}
