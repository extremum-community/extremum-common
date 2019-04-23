package com.extremum.common.dao;

import com.extremum.common.cofnig.ElasticDaoConfig;
import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.factory.impl.ElasticDescriptorFactory;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.exceptions.CommonException;
import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.common.models.ElasticData;
import com.extremum.common.models.PersistableCommonModel.FIELDS;
import com.extremum.common.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class DefaultElasticCommonDao implements ElasticCommonDao<ElasticData> {
    private ElasticDescriptorFactory elasticDescriptorFactory;

    private int taken = 0x0;
    private ElasticDaoConfig daoConfig;

    public DefaultElasticCommonDao(ElasticDaoConfig config, ElasticDescriptorFactory descriptorFactory) {
        daoConfig = config;
        this.elasticDescriptorFactory = descriptorFactory;
    }

    private synchronized ElasticData updateServiceFields(ElasticData model) {
        try {
            ZonedDateTime now = ZonedDateTime.now();

            JSONObject newObject = new JSONObject(model.getRawDocument());

            newObject.put(FIELDS.id.name(), model.getId());

            if (!newObject.has(FIELDS.created.name())) {
                model.setCreated(now);
                newObject.put(FIELDS.created.name(), DateUtils.convert(now));
            }

            newObject.put(FIELDS.modified.name(), DateUtils.convert(now));
            model.setModified(now);


            long oldVersion = 0;
            if (newObject.has(FIELDS.version.name())) {
                oldVersion = newObject.getLong(FIELDS.version.name());
            }
            long newVersion = oldVersion + 1;
            newObject.put(FIELDS.version.name(), newVersion);
            model.setVersion(newVersion);

            if (!newObject.has(FIELDS.deleted.name())) {
                newObject.put(FIELDS.deleted.name(), Boolean.FALSE);
                model.setDeleted(false);
            }

            model.setRawDocument(newObject.toString());

            return model;
        } catch (JSONException e) {
            log.error("Can't update model {}", model, e);
            throw new CommonException(e, "Can't update model " + model, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public List<ElasticData> search(String filter, String path, String query) {
        String currentElastic = nextUrl();
        String url = currentElastic + "/" + daoConfig.getEndpoint() + "/" +
                daoConfig.getSearchPrefix() + daoConfig.getScrollSize() + "&scroll=" + daoConfig.getScrollKeepAlive();

        log.debug("Search with filter {} using path {} and query {}. Full URL is {}", filter, path, query, url);

        HttpRequestBase request = buildQuery(filter, path, query)
                .map(q -> {
                    HttpEntity queryEntity = new StringEntity(q, ContentType.APPLICATION_JSON);
                    HttpPost post = new HttpPost(url);
                    post.setEntity(queryEntity);
                    return (HttpRequestBase) post;
                })
                .orElseGet(() -> new HttpGet(url));

        String response = null;
        int maxRetries = daoConfig.getMaxRetries();

        while (response == null) {
            try {
                HttpResponse httpResponse = daoConfig.getClient().execute(request);
                response = EntityUtils.toString(httpResponse.getEntity());

                if (isBadGateway(response)) {
                    log.error("Got bad gateway on initial request");
                    if (maxRetries-- > 0) {
                        response = null;
                    } else {
                        throw new RuntimeException("Elastic not behaving: " + response);
                    }
                }
            } catch (IOException e) {
                //ignore
            } finally {
                request.releaseConnection();
            }
        }

        try {
            JSONObject responseJson = new JSONObject(response);
            if (this.hitsAreEmpty(responseJson)) {
                log.info("No hits found for " + daoConfig.getEndpoint() + "/" + daoConfig.getSearchPrefix() +
                        daoConfig.getScrollSize() + "&scroll=" + daoConfig.getScrollKeepAlive());
                return null;
            }
            JSONArray hits = responseJson.getJSONObject("hits").getJSONArray("hits");

            List<ElasticData> result = new ArrayList<>();

            for (int i = 0x0; i < hits.length(); i++) {
                ElasticData data = getElasticDataFromRawDocument(hits.getJSONObject(i).toString());
                result.add(data);
            }

            return result;
        } catch (JSONException e) {
            throw new RuntimeException("Error parsing json : " + response);
        }
    }

    private boolean isBadGateway(String response) {
        return response.startsWith("<html>") && response.contains("Bad Gateway");
    }

    private boolean hitsAreEmpty(JSONObject scrollJson) throws JSONException {
        return !scrollJson.has("hits") || scrollJson.getJSONObject("hits").getJSONArray("hits").length() == 0;
    }

    private synchronized String nextUrl() {
        int nextUrlIdx = taken++ % daoConfig.getElasticUrlList().size();
        if (taken > Integer.MAX_VALUE - 1) {
            taken = 0;
        }
        String nextUrl = daoConfig.getElasticUrlList().get(nextUrlIdx);

        String selected = nextUrl != null ? nextUrl : daoConfig.getElasticUrlList().get(0);

        log.debug("Next URL selected {}", selected);

        return selected;
    }

    private Optional<String> buildQuery(String filter, String path, String query) {
        return query == null ? Optional.empty() : Optional.of(String.format(filter, query));
    }

    @Override
    public List<ElasticData> findAll() {
        log.warn("Please use the findById() method or search() method. " +
                "Method findAll() may produce very large data response");
        return Collections.emptyList();
    }

    @Override
    public ElasticData findById(String id) {
        String uri = buildUrl(id);

        log.debug("Get model {} with id {}", daoConfig.getEndpoint(), id);

        HttpGet get = new HttpGet(uri);
        try {
            HttpResponse response = daoConfig.getClient().execute(get);
            if (response != null && response.getStatusLine() != null) {
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == HttpStatus.SC_OK) {
                    String entity = EntityUtils.toString(response.getEntity());

                    return getElasticDataFromRawDocument(entity);
                } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                    throw new ModelNotFoundException(ElasticData.class, id);
                } else {
                    log.warn("Can't get data by id {}, response is {}", id, response);
                    return null;
                }
            }
        } catch (IOException e) {
            log.error("Can't get a model with id {} from endpoint {}", id, daoConfig.getEndpoint(), e);
            throw new RuntimeException(e);
        } finally {
            get.releaseConnection();
        }

        return null;
    }

    private List<ElasticData> getElasticDataFromRawDocuments(List<String> rawDocuments) throws JSONException {
        return rawDocuments.stream().map(this::getElasticDataFromRawDocument).collect(Collectors.toList());
    }

    private ElasticData getElasticDataFromRawDocument(String rawDocument) {
        try {
            JSONObject json = new JSONObject(rawDocument);
            JSONObject result = json.getJSONObject("_source");

            ElasticData.ElasticDataBuilder builder = ElasticData.builder();

            if (json.has("_" + FIELDS.version.name())) {
                long version = json.getLong("_" + FIELDS.version.name());
                result.put(FIELDS.version.name(), version);
                builder.version(version);
            }

            String foundDocument = result.toString();
            builder.rawDocument(foundDocument);

            String internalId = result.getString(FIELDS.id.name());
            builder.id(internalId);
            builder.uuid(DescriptorService.loadByInternalId(internalId).get());

            if (result.has(FIELDS.created.name())) {
                String rawCreated = result.getString(FIELDS.created.name());
                builder.created(DateUtils.parseZonedDateTime(rawCreated, DateUtils.ISO_8601_ZONED_DATE_TIME_FORMATTER));
            }

            if (result.has(FIELDS.modified.name())) {
                String rawModified = result.getString(FIELDS.modified.name());
                builder.modified(DateUtils.parseZonedDateTime(rawModified, DateUtils.ISO_8601_ZONED_DATE_TIME_FORMATTER));
            }

            return builder.build();
        } catch (JSONException e) {
            log.error("Unable to transform raw document to ElasticData", e);
            throw new RuntimeException("Unable to transform raw document to ElasticData", e);
        }
    }

    @Override
    public ElasticData findById(String id, String... includeFields) {
        log.warn("The search will be performed only by ID without taking into account the includeFields parameter");
        return findById(id);
    }

    @Override
    public boolean isExists(String id) {
        String url = buildUrl(id);

        log.debug("Checking existing id: " + id);

        HttpHead head = new HttpHead(url);
        String response = null;
        int maxRetries = daoConfig.getMaxRetries();

        try {
            while (response == null) {
                HttpResponse httpResponse = daoConfig.getClient().execute(head);

                if (httpResponse.getEntity() != null) {
                    response = EntityUtils.toString(httpResponse.getEntity());
                    if (isBadGateway(response)) {
                        log.error("Got bad gateway on initial request");
                        if (maxRetries-- > 0) {
                            response = null;
                        } else {
                            throw new RuntimeException("Elastic not behaving: " + response);
                        }
                    }
                } else {
                    return httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Elastic not behaving");
        } finally {
            head.releaseConnection();
        }

        return true;
    }

    @Override
    public boolean isDeleted(String id) {
        return findById(id).getDeleted();
    }

    @Override
    public ElasticData create(ElasticData model) {
        if (model.getId() == null) {
            Descriptor descriptor = elasticDescriptorFactory.create(UUID.randomUUID(), model.getModelName());
            Descriptor stored = DescriptorService.store(descriptor);
            model.setUuid(stored);
            model.setId(stored.getInternalId());
        }

        return persist(model);
    }

    @Override
    public ElasticData persist(ElasticData model) {
        log.debug("Persisting {}", model);

        if (model.getId() == null) {
            return create(model);
        } else {
            String externalId = model.getUuid().getExternalId();

            String uri = buildUrl(model.getId());
            if (model.getVersion() != null) {
                uri += "?version=" + model.getVersion();
            }

            log.debug("Selected elastic uri {}", uri);

            HttpPut httpPut = new HttpPut(uri);

            ElasticData updateData = updateServiceFields(model);

            try {
                StringEntity entity = new StringEntity(updateData.getRawDocument());
                httpPut.setEntity(entity);
                httpPut.setHeader("Accept", "application/json");
                httpPut.setHeader("Content-type", "application/json");

                HttpResponse response = daoConfig.getClient().execute(httpPut);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CONFLICT) {
                    log.error("Concurrent modification of model with id {} detected", externalId);
                    throw new ConcurrentModificationException("Concurrent modification of model with id " + externalId);
                }

                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED &&
                        response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    log.error("Unable to update elasticsearch entity, because response status is {}",
                            response.getStatusLine());
                    throw new RuntimeException("Unable to update elasticsearch entity '" + model.getId() + "'");
                }

                return updateData;
            } catch (IOException e) {
                log.error("Unable to update elasticsearch entity", e);
                throw new RuntimeException("Unable to update elasticsearch entity '" + model.getId() + "'");
            } finally {
                httpPut.releaseConnection();
            }
        }
    }

    @Override
    public boolean remove(String id) {
        return false;
    }

    private String buildUrl(String id) {
        return String.join(
                "/",
                nextUrl(),
                daoConfig.getEndpoint(),
                id
        );
    }
}
