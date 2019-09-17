package io.extremum.elasticsearch.springdata.reactiverepository;

import io.extremum.elasticsearch.facilities.ReactiveElasticsearchDescriptorFacilities;
import io.extremum.elasticsearch.springdata.repository.SequenceNumberOperations;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.index.VersionType;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsMapper;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class ExtremumReactiveElasticsearchTemplate extends ReactiveElasticsearchTemplate {
    private final ElasticsearchConverter converter;
    private final ResultsMapper resultMapper;

    private final ReactiveSaveProcess saveProcess;
    private final SequenceNumberOperations sequenceNumberOperations = new SequenceNumberOperations();

    public ExtremumReactiveElasticsearchTemplate(ReactiveElasticsearchClient client,
                                                 ElasticsearchConverter converter,
                                                 ResultsMapper resultMapper,
                                                 ReactiveElasticsearchDescriptorFacilities descriptorFacilities) {
        super(client, converter, resultMapper);

        this.converter = converter;
        this.resultMapper = resultMapper;

        saveProcess = new ReactiveSaveProcess(descriptorFacilities);
    }

    @Override
    public <T> Mono<T> save(T entity, @Nullable String index, @Nullable String type) {
        Assert.notNull(entity, "Entity must not be null!");

        return saveProcess.prepareForSave(entity)
                .then(doIndex(entity, index, type))
                .flatMap(indexResponse -> populateEntityAfterSave(entity, indexResponse));
    }

    private <T> Mono<T> populateEntityAfterSave(T entity, IndexResponse indexResponse) {
        populateIdIfNecessary(entity, indexResponse.getId());

        return saveProcess.fillAfterSave(entity, indexResponse)
                .thenReturn(entity);
    }

    private <T> Mono<IndexResponse> doIndex(T value, @Nullable String index, @Nullable String type) {
        @SuppressWarnings("unchecked")
        ElasticsearchPersistentEntity<T> entity = getPersistentEntityFor(value.getClass());
        ConvertingPropertyAccessor<T> propertyAccessor = new ConvertingPropertyAccessor<>(
                entity.<T>getPropertyAccessor(value), converter.getConversionService());

        final String effectiveIndex = index != null ? index : entity.getIndexName();
        final String effectiveType = type != null ? type : entity.getIndexType();

        return Mono.defer(() -> {
            Object id = entity.getIdentifierAccessor(value).getIdentifier();
            IndexRequest request = id != null
                    ? new IndexRequest(effectiveIndex, effectiveType, converter.convertId(id))
                    : new IndexRequest(effectiveIndex, effectiveType);

            try {
                request.source(resultMapper.getEntityMapper().mapToString(value), Requests.INDEX_CONTENT_TYPE);
            } catch (IOException var9) {
                throw new RuntimeException(var9);
            }

            if (entity.hasVersionProperty()) {
                Object version = propertyAccessor.getProperty(entity.getRequiredVersionProperty());
                if (version != null) {
                    request.version(((Number)version).longValue());
                    request.versionType(VersionType.EXTERNAL);
                }
            }

            request = this.prepareIndexRequest(value, request);
            return this.doIndex(request);
        });
    }

    private ElasticsearchPersistentEntity getPersistentEntityFor(Class clazz) {
        Assert.isTrue(clazz.isAnnotationPresent(Document.class),
                "Unable to identify index name. " + clazz.getSimpleName()
                        + " is not a Document. Make sure the document class is annotated with " +
                        "@Document(indexName=\"foo\")");

        return converter.getMappingContext().getRequiredPersistentEntity(clazz);
    }

    private <T> void populateIdIfNecessary(T bean, @Nullable Object id) {
        if (id == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        ElasticsearchPersistentEntity<T> entity = getPersistentEntityFor(bean.getClass());
        ConvertingPropertyAccessor<T> propertyAccessor = new ConvertingPropertyAccessor<>(
                entity.<T>getPropertyAccessor(bean), converter.getConversionService());

        ElasticsearchPersistentProperty idProperty = entity.getIdProperty();
        propertyAccessor.setProperty(idProperty, id);
    }

    @Override
    protected IndexRequest prepareIndexRequest(Object source, IndexRequest request) {
        IndexRequest preparedRequest = super.prepareIndexRequest(source, request);

        sequenceNumberOperations.fillSequenceNumberAndPrimaryTermOnIndexRequest(source, preparedRequest);

        return preparedRequest;
    }
}
