package com.extremum.elasticsearch.repositories;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.VersionType;
import org.springframework.data.elasticsearch.ElasticsearchException;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.EntityMapper;
import org.springframework.data.elasticsearch.core.ResultsMapper;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @author rpuch
 */
public class ExtremumElasticsearchRestTemplate extends ElasticsearchRestTemplate {
    private final SequenceNumberOperations sequenceNumberOperations = new SequenceNumberOperations();
    private final VersionOperations versionOperations = new VersionOperations();
    private final ManualAuditing manualAuditing = new ManualAuditing();

    public ExtremumElasticsearchRestTemplate(RestHighLevelClient client) {
        super(client);
    }

    public ExtremumElasticsearchRestTemplate(RestHighLevelClient client,
            EntityMapper entityMapper) {
        super(client, entityMapper);
    }

    public ExtremumElasticsearchRestTemplate(RestHighLevelClient client,
            ElasticsearchConverter elasticsearchConverter,
            EntityMapper entityMapper) {
        super(client, elasticsearchConverter, entityMapper);
    }

    public ExtremumElasticsearchRestTemplate(RestHighLevelClient client,
            ResultsMapper resultsMapper) {
        super(client, resultsMapper);
    }

    public ExtremumElasticsearchRestTemplate(RestHighLevelClient client,
            ElasticsearchConverter elasticsearchConverter) {
        super(client, elasticsearchConverter);
    }

    public ExtremumElasticsearchRestTemplate(RestHighLevelClient client,
            ElasticsearchConverter elasticsearchConverter,
            ResultsMapper resultsMapper) {
        super(client, elasticsearchConverter, resultsMapper);
    }

    @Override
    public String index(IndexQuery query) {
        if (query.getObject() != null) {
            manualAuditing.fillCreatedAndModifiedDates(query.getObject());
        }

        String documentId;
        IndexRequest request = prepareIndex(query);
        IndexResponse response;
        try {
            response = getClient().index(request);
            documentId = response.getId();
        } catch (IOException e) {
            throw new ElasticsearchException("Error while index for request: " + request.toString(), e);
        }
        // We should call this because we are not going through a mapper.
        if (query.getObject() != null) {
            setPersistentEntityId(query.getObject(), documentId);
            sequenceNumberOperations.setSequenceNumberAndPrimaryTermAfterIndexing(query.getObject(), response);
            versionOperations.setVersionAfterIndexing(query.getObject(), response);
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

            if (query.getParentId() != null) {
                indexRequest.parent(query.getParentId());
            }

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
}
