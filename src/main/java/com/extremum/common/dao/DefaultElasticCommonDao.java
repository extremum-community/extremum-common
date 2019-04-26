package com.extremum.common.dao;

import com.extremum.common.dao.extractor.AccessorFacade;
import com.extremum.common.dao.extractor.GetResponseAccessorFacade;
import com.extremum.common.dao.extractor.SearchHitAccessorFacade;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.impl.ElasticDescriptorFactory;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.exceptions.CommonException;
import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.common.models.ElasticData;
import com.extremum.common.models.PersistableCommonModel.FIELDS;
import com.extremum.common.utils.CollectionUtils;
import com.extremum.common.utils.DateUtils;
import com.extremum.starter.properties.ElasticProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

@Slf4j
public class DefaultElasticCommonDao implements ElasticCommonDao<ElasticData> {
    private static final String DELETE_DOCUMENT_PAINLESS_SCRIPT = "ctx._source.deleted = params.deleted; ctx._source.modified = params.modified";

    private RestClientBuilder restClientBuilder;
    private final ElasticDescriptorFactory elasticDescriptorFactory;

    private final ElasticProperties elasticProps;
    private final String indexName;
    private final String indexType;

    public DefaultElasticCommonDao(ElasticProperties elasticProperties, ElasticDescriptorFactory descriptorFactory,
                                   String indexName, String indexType) {
        this.elasticProps = elasticProperties;
        this.elasticDescriptorFactory = descriptorFactory;
        this.indexName = indexName;
        this.indexType = indexType;

        initRest();
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

    private synchronized ElasticData updateServiceFields(ElasticData model) {
        try {
            if (model.getModified() == null) {
                model.setModified(model.getCreated());
            }

            JSONObject json = new JSONObject(model.getRawDocument());

            json.put(FIELDS.id.name(), model.getId());
            json.put(FIELDS.created.name(), DateUtils.formatZonedDateTimeISO_8601(model.getCreated()));
            json.put(FIELDS.modified.name(), DateUtils.formatZonedDateTimeISO_8601(model.getModified()));
            json.put(FIELDS.deleted.name(), model.getDeleted());

            model.setRawDocument(json.toString());

            return model;
        } catch (JSONException e) {
            log.error("Can't update model {}", model, e);
            throw new CommonException(e, "Can't update model " + model, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<ElasticData> search(String queryString) {
        final SearchRequest request = new SearchRequest(indexName);

        request.source(
                new SearchSourceBuilder()
                        .query(
                                QueryBuilders.queryStringQuery(queryString)));

        try (RestHighLevelClient client = getClient()) {
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);

            if (HttpStatus.SC_OK == response.status().getStatus()) {
                List<ElasticData> results = new ArrayList<>();

                for (SearchHit hit : response.getHits()) {
                    ElasticData data = extract(new SearchHitAccessorFacade(hit));
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
    public List<ElasticData> findAll() {
        log.warn("Please use the findById() method or search() method. " +
                "Method findAll() may produce very large data response");
        return Collections.emptyList();
    }

    @Override
    public ElasticData findById(String id) {
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
                    return extract(new GetResponseAccessorFacade(response));
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
    public ElasticData findById(String id, String... includeFields) {
        log.warn("The search will be performed only by ID without taking into account the includeFields parameter");
        return findById(id);
    }

    @Override
    public boolean isExists(String id) {
        if (isDocumentExists(id)) {
            try {
                final ElasticData data = findById(id);
                return !data.getDeleted();
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

    @Override
    public ElasticData create(ElasticData model) {
        return persist(model);
    }

    @Override
    public ElasticData persist(ElasticData model, Long seqNo, Long primaryTerm) {
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

        final ElasticData updatedModel = updateServiceFields(model);

        try (RestHighLevelClient client = getClient()) {
            final IndexRequest request = new IndexRequest();
            request.index(indexName);
            request.id(model.getId());

            request.source(updatedModel.getRawDocument(), XContentType.JSON);

            if (seqNo != null && primaryTerm != null) {
                request.setIfSeqNo(seqNo);
                request.setIfPrimaryTerm(primaryTerm);
            }

            final IndexResponse response = client.index(request, RequestOptions.DEFAULT);

            if (asList(SC_OK, SC_CREATED).contains(response.status().getStatus())) {
                model.setSeqNo(response.getSeqNo());
                model.setPrimaryTerm(response.getPrimaryTerm());
                return updatedModel;
            } else {
                log.error("Document don't be indexed, status {}", response.status());
                throw new RuntimeException("Document don't be indexed");
            }
        } catch (IOException e) {
            log.error("Unable to add data to index", e);
            throw new RuntimeException("Unable to add data to index", e);
        }
    }

    @Override
    public ElasticData persist(ElasticData model) {
        return persist(model, null, null);
    }

    @Override
    public boolean remove(String id) {
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
    }

    private ElasticData extract(AccessorFacade accessor) {
        final ElasticData data = ElasticData.builder()
                .id(accessor.getId())
                .uuid(accessor.getUuid())
                .seqNo(accessor.getSeqNo())
                .primaryTerm(accessor.getPrimaryTerm())
                .version(accessor.getVersion())
                .deleted(accessor.getDeleted())
                .created(accessor.getCreated())
                .modified(accessor.getModified())
                .rawDocument(accessor.getRawSource())
                .build();

        ofNullable(data.getUuid())
                .map(Descriptor::getModelType)
                .ifPresent(data::setModelName);

        return data;
    }
}
