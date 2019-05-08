package com.extremum.common.dao;

import com.extremum.common.dao.extractor.AccessorFacade;
import com.extremum.common.dao.extractor.GetResponseAccessorFacade;
import com.extremum.common.dao.extractor.SearchHitAccessorFacade;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.impl.ElasticDescriptorFactory;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.common.models.ElasticCommonModel;
import com.extremum.common.models.PersistableCommonModel.FIELDS;
import com.extremum.common.utils.CollectionUtils;
import com.extremum.common.utils.DateUtils;
import com.extremum.starter.properties.ElasticProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

@Slf4j
public class DefaultElasticCommonDao<Model extends ElasticCommonModel> implements ElasticCommonDao<Model> {
    private static final String DELETE_DOCUMENT_PAINLESS_SCRIPT = "ctx._source.deleted = params.deleted; ctx._source.modified = params.modified";

    private RestClientBuilder restClientBuilder;
    private ElasticDescriptorFactory elasticDescriptorFactory;

    private ElasticProperties elasticProps;
    private ObjectMapper mapper;
    private String indexName;
    private String indexType;

    private Class<? extends Model> modelClass;

    public DefaultElasticCommonDao(Class<Model> modelClass, ElasticProperties elasticProperties,
                                   ElasticDescriptorFactory descriptorFactory, ObjectMapper mapper, String indexName,
                                   String indexType) {
        this.modelClass = modelClass;
        this.elasticProps = elasticProperties;
        this.elasticDescriptorFactory = descriptorFactory;
        this.mapper = mapper;
        this.indexName = indexName;
        this.indexType = indexType;

        initRest();
    }

    protected DefaultElasticCommonDao(ElasticProperties elasticProperties, ElasticDescriptorFactory descriptorFactory,
                                      ObjectMapper mapper, String indexName, String indexType) {
        this.elasticProps = elasticProperties;
        this.elasticDescriptorFactory = descriptorFactory;
        this.mapper = mapper;
        this.indexName = indexName;
        this.indexType = indexType;

        initRest();
        initModelClass();
    }

    protected void initModelClass() {
        modelClass = (Class<Model>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected void initRest() {
        if (CollectionUtils.isNullOrEmpty(elasticProps.getHosts())) {
            log.error("Unable to configure {} because list of hosts is empty", RestClientBuilder.class.getName());
            throw new RuntimeException("Unable to configure " + RestClientBuilder.class.getName() +
                    " because list of hosts is empty");
        } else {
            List<HttpHost> httpHosts = elasticProps.getHosts().stream()
                    .map(h -> new HttpHost(h.getHost(), h.getPort(), h.getProtocol()))
                    .collect(Collectors.toList());

            this.restClientBuilder = RestClient.builder(httpHosts.toArray(new HttpHost[]{}));

            if (elasticProps.getUsername() != null && elasticProps.getPassword() != null) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(elasticProps.getUsername(), elasticProps.getPassword()));

                this.restClientBuilder.setHttpClientConfigCallback(httpAsyncClientBuilder ->
                        httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
            }
        }
    }

    private RestHighLevelClient getClient() {
        return new RestHighLevelClient(restClientBuilder);
    }

    @Override
    public List<Model> search(String queryString) {
        final SearchRequest request = new SearchRequest(indexName);

        request.source(
                new SearchSourceBuilder()
                        .query(
                                QueryBuilders.queryStringQuery(queryString)));

        return doSearch(queryString, request);
    }

    protected List<Model> doSearch(String queryString, SearchRequest request) {
        try (RestHighLevelClient client = getClient()) {
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);

            if (HttpStatus.SC_OK == response.status().getStatus()) {
                List<Model> results = new ArrayList<>();

                for (SearchHit hit : response.getHits()) {
                    Model data = extract(new SearchHitAccessorFacade(hit));
                    results.add(data);
                }

                return results;
            } else {
                log.error("Unable to perform search by query {}: {}", queryString, response.status());
                throw new RuntimeException("Nothing found by query " + queryString);
            }
        } catch (IOException e) {
            log.error("Unable to search by query {}", queryString, e);
            throw new RuntimeException("Unable to search by query " + queryString, e);
        }
    }

    @Override
    public List<Model> findAll() {
        log.warn("Please use the findById() method or search() method. " +
                "Method findAll() may produce very large data response");
        return Collections.emptyList();
    }

    @Override
    public Optional<Model> findById(String id) {
        try (RestHighLevelClient client = getClient()) {
            GetResponse response = client.get(
                    new GetRequest(indexName, id),
                    RequestOptions.DEFAULT
            );

            if (response.isExists()) {
                Map<String, Object> sourceMap = response.getSourceAsMap();

                if (sourceMap.getOrDefault(FIELDS.deleted.name(), Boolean.FALSE).equals(Boolean.TRUE)) {
                    throw new ModelNotFoundException("Not found " + id);
                } else {
                    return Optional.ofNullable(extract(new GetResponseAccessorFacade(response)));
                }
            } else {
                throw new ModelNotFoundException("Not found " + id);
            }
        } catch (IOException e) {
            log.error("Unable to get data by id {} from index {} with type {}",
                    id, indexName, indexType, e);
            throw new RuntimeException("Unable to get data by id " + id +
                    " from index " + indexName +
                    " with type " + indexType,
                    e);
        }
    }

    @Override
    public boolean existsById(String id) {
        if (isDocumentExists(id)) {
            try {
                final Optional<Model> data = findById(id);
                boolean doesNotExist = data.map(ElasticCommonModel::getDeleted).orElse(true);
                return !doesNotExist;
            } catch (ModelNotFoundException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    protected boolean isDocumentExists(String id) {
        try (RestHighLevelClient client = getClient()) {
            GetRequest getRequest = new GetRequest(indexName, id);
            getRequest.fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE);
            getRequest.storedFields("_none_");

            GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);
            return response.isExists();
        } catch (IOException e) {
            log.error("Unable to check exists data by id {}", id, e);
            throw new RuntimeException("Unable to check data exists by id " + id, e);
        }
    }

    @Override
    public boolean isDeleted(String id) {
        try (RestHighLevelClient client = getClient()) {
            GetRequest getRequest = new GetRequest(indexName, id);
            getRequest.storedFields("_none_");
            GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);

            if (response.isExists()) {
                return (boolean) response.getSourceAsMap().getOrDefault(FIELDS.deleted.name(), Boolean.TRUE);
            } else {
                return true;
            }
        } catch (IOException e) {
            log.error("Unable to check deleted data by id {}", id, e);
            throw new RuntimeException("Unable to check deleted data by id " + id, e);
        }
    }

//    @Override
//    public Model create(Model model) {
//        return save(model);
//    }

    @Override
    public <N extends Model> N save(N model) {
        preSave(model);

        return doSave(model);
    }

    protected <N extends Model> N doSave(N model) {
        String rawData = serializeModel(model);

        try (RestHighLevelClient client = getClient()) {
            final IndexRequest request = new IndexRequest();
            request.index(indexName);
            request.id(model.getId());

            request.source(rawData, XContentType.JSON);

            if (model.getSeqNo() != null && model.getPrimaryTerm() != null) {
                request.setIfSeqNo(model.getSeqNo());
                request.setIfPrimaryTerm(model.getPrimaryTerm());
            }

            final IndexResponse response = client.index(request, RequestOptions.DEFAULT);

            if (asList(SC_OK, SC_CREATED).contains(response.status().getStatus())) {
                model.setSeqNo(response.getSeqNo());
                model.setPrimaryTerm(response.getPrimaryTerm());
                return model;
            } else {
                log.error("Document don't be indexed, status {}", response.status());
                throw new RuntimeException("Document don't be indexed");
            }
        } catch (IOException e) {
            log.error("Unable to add data to index", e);
            throw new RuntimeException("Unable to add data to index", e);
        }
    }

    protected void preSave(Model model) {
        if (model.getId() == null) {
            final Descriptor descriptor = elasticDescriptorFactory.create(UUID.randomUUID(), model.getModelName());
            final Descriptor stored = DescriptorService.store(descriptor);
            model.setUuid(stored);
            model.setId(stored.getInternalId());
            model.setCreated(ZonedDateTime.now());
            model.setVersion(0L);
            model.setDeleted(Boolean.FALSE);
        } else {
            if (isDeleted(model.getId())) {
                throw new RuntimeException("Document " + model.getId() + " has been deleted and can't be updated");
            } else {
                model.setModified(ZonedDateTime.now());
            }
        }
    }

    private String serializeModel(Model model) {
        try {
            return mapper.writeValueAsString(model);
        } catch (JsonProcessingException e) {
            log.error("Unable to serialize model {}", model, e);
            throw new RuntimeException("Unable to serialize model " + model, e);
        }
    }

    private Model deserializeModel(String rawSource) {
        try {
            return mapper.readValue(rawSource, modelClass);
        } catch (IOException e) {
            log.error("Unable to deserialize {} to {}", rawSource, modelClass, e);
            throw new RuntimeException("Unable to deserialize " + rawSource + " to " + modelClass, e);
        }
    }

    @Override
    public <N extends Model> Iterable<N> saveAll(Iterable<N> entities) {
        entities.forEach(this::save);
        return entities;
    }

    @Override
    public boolean softDeleteById(String id) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(FIELDS.deleted.name(), Boolean.TRUE);
        parameters.put(FIELDS.modified.name(), DateUtils.formatZonedDateTimeISO_8601(ZonedDateTime.now()));

        return patch(id, DELETE_DOCUMENT_PAINLESS_SCRIPT, parameters);
    }

    @Override
    public boolean patch(String id, String painlessQuery) {
        return patch(id, painlessQuery, null);
    }

    @Override
    public boolean patch(String id, String painlessScript, Map<String, Object> params) {
        if (existsById(id)) {
            final UpdateRequest request = new UpdateRequest(indexName, id);

            request.script(new Script(ScriptType.INLINE, "painless", painlessScript, params));

            try (final RestHighLevelClient client = getClient()) {
                final UpdateResponse response = client.update(request, RequestOptions.DEFAULT);

                if (SC_OK == response.status().getStatus()) {
                    return true;
                } else {
                    log.warn("Document {} is not patched, status {}", id, response.status());
                    return false;
                }
            } catch (IOException e) {
                log.error("Unable to patch document {}", id, e);
                throw new RuntimeException("Unable to patch document " + id, e);
            }
        } else {
            throw new ModelNotFoundException("Not found " + id);
        }
    }

    private Model extract(AccessorFacade accessor) {
        Model model = deserializeModel(accessor.getRawSource());
        model.setId(accessor.getId());
        model.setUuid(accessor.getUuid());
        model.setVersion(accessor.getVersion());
        model.setSeqNo(accessor.getSeqNo());
        model.setPrimaryTerm(accessor.getPrimaryTerm());

        final Map<String, Object> sourceAsMap = accessor.getSourceAsMap();

        final Boolean deleted = ofNullable(sourceAsMap)
                .map(m -> m.get(FIELDS.deleted.name()))
                .map(Boolean.class::cast)
                .orElse(Boolean.FALSE);

        model.setDeleted(deleted);

        ofNullable(sourceAsMap)
                .map(m -> zonedDateTimeFromMap(m, FIELDS.created.name()))
                .ifPresent(model::setCreated);

        ofNullable(sourceAsMap)
                .map(m -> zonedDateTimeFromMap(m, FIELDS.modified.name()))
                .ifPresent(model::setModified);

        return model;
    }


    private ZonedDateTime zonedDateTimeFromMap(Map<String, Object> map, String fieldName) {
        return ofNullable(map)
                .map(m -> m.get(fieldName))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(v -> DateUtils.parseZonedDateTime(v, DateUtils.ISO_8601_ZONED_DATE_TIME_FORMATTER))
                .orElse(null);
    }
}
