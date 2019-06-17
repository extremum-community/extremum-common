package com.extremum.elasticsearch.repositories;

import com.extremum.elasticsearch.model.ElasticsearchCommonModel;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.index.seqno.SequenceNumbers;
import org.elasticsearch.search.SearchHit;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.ElasticsearchException;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.ScriptedField;
import org.springframework.data.elasticsearch.core.DefaultResultMapper;
import org.springframework.data.elasticsearch.core.EntityMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.extremum.elasticsearch.repositories.ElasticsearchModels.asElasticsearchModel;

/**
 * @author rpuch
 */
public class ExtremumResultMapper extends DefaultResultMapper {
    private final MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext;
    private final ConversionService conversionService = new DefaultConversionService();

    public ExtremumResultMapper(EntityMapper entityMapper) {
        this(new SimpleElasticsearchMappingContext(), entityMapper);
    }

    public ExtremumResultMapper(
            MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext,
            EntityMapper entityMapper) {
        super(mappingContext, entityMapper);

        this.mappingContext = mappingContext;
    }

    @Override
    public <T> T mapResult(GetResponse response, Class<T> clazz) {
        T result = super.mapResult(response, clazz);

        asElasticsearchModel(result).ifPresent(model -> {
            fillSequenceNumberAndPrimaryTerm(response, model);
        });
        
        return result;
    }

    private void fillSequenceNumberAndPrimaryTerm(GetResponse response, ElasticsearchCommonModel model) {
        fillSequenceNumberAndPrimaryTerm(response.getSeqNo(), response.getPrimaryTerm(), model);
    }

    private void fillSequenceNumberAndPrimaryTerm(long seqNo, long primaryTerm, ElasticsearchCommonModel model) {
        if (seqNo != SequenceNumbers.UNASSIGNED_SEQ_NO) {
            model.setSeqNo(seqNo);
        }
        if (primaryTerm != SequenceNumbers.UNASSIGNED_PRIMARY_TERM) {
            model.setPrimaryTerm(primaryTerm);
        }
    }

    // The following is needed to work-around an incompatible class change in Elasticsearch client libraries
    // which makes spring-data-elasticsearch 3.2 fail.

    @Override
    public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
        long totalHits = response.getHits().getTotalHits().value;
        float maxScore = response.getHits().getMaxScore();

        List<T> results = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            if (hit != null) {
                T result;
                String hitSourceAsString = hit.getSourceAsString();
                if (!StringUtils.isEmpty(hitSourceAsString)) {
                    result = mapEntity(hitSourceAsString, clazz);
                } else {
                    result = mapEntity(hit.getFields().values(), clazz);
                }

                setPersistentEntityId(result, hit.getId(), clazz);
                setPersistentEntityVersion(result, hit.getVersion(), clazz);
                setPersistentEntityScore(result, hit.getScore(), clazz);

                populateScriptFields(result, hit);

                // we also add filling of seqNo and primaryTerm
                asElasticsearchModel(result).ifPresent(model -> {
                    fillSequenceNumberAndPrimaryTerm(hit.getSeqNo(), hit.getPrimaryTerm(), model);
                });

                results.add(result);
            }
        }

        return new AggregatedPageImpl<>(results, pageable, totalHits, response.getAggregations(),
                response.getScrollId(),
                maxScore);
    }

    private <T> T mapEntity(Collection<DocumentField> values, Class<T> clazz) {
        return mapEntity(buildJSONFromFields(values), clazz);
    }

    private String buildJSONFromFields(Collection<DocumentField> values) {
        JsonFactory nodeFactory = new JsonFactory();
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            JsonGenerator generator = nodeFactory.createGenerator(stream, JsonEncoding.UTF8);
            generator.writeStartObject();
            for (DocumentField value : values) {
                if (value.getValues().size() > 1) {
                    generator.writeArrayFieldStart(value.getName());
                    for (Object val : value.getValues()) {
                        generator.writeObject(val);
                    }
                    generator.writeEndArray();
                } else {
                    generator.writeObjectField(value.getName(), value.getValue());
                }
            }
            generator.writeEndObject();
            generator.flush();
            return new String(stream.toByteArray(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            return null;
        }
    }

    private <T> void setPersistentEntityId(T result, String id, Class<T> clazz) {

        if (clazz.isAnnotationPresent(Document.class)) {

            ElasticsearchPersistentEntity<?> persistentEntity = mappingContext.getRequiredPersistentEntity(clazz);
            ElasticsearchPersistentProperty idProperty = persistentEntity.getIdProperty();

            PersistentPropertyAccessor<T> accessor = new ConvertingPropertyAccessor<>(
                    persistentEntity.getPropertyAccessor(result), conversionService);

            // Only deal with String because ES generated Ids are strings !
            if (idProperty != null && idProperty.getType().isAssignableFrom(String.class)) {
                accessor.setProperty(idProperty, id);
            }
        }
    }

    private <T> void setPersistentEntityVersion(T result, long version, Class<T> clazz) {

        if (clazz.isAnnotationPresent(Document.class)) {

            ElasticsearchPersistentEntity<?> persistentEntity = mappingContext.getPersistentEntity(clazz);
            ElasticsearchPersistentProperty versionProperty = persistentEntity.getVersionProperty();

            // Only deal with Long because ES versions are longs !
            if (versionProperty != null && versionProperty.getType().isAssignableFrom(Long.class)) {
                // check that a version was actually returned in the response, -1 would indicate that
                // a search didn't request the version ids in the response, which would be an issue
                Assert.isTrue(version != -1, "Version in response is -1");
                persistentEntity.getPropertyAccessor(result).setProperty(versionProperty, version);
            }
        }
    }

    private <T> void setPersistentEntityScore(T result, float score, Class<T> clazz) {

        if (clazz.isAnnotationPresent(Document.class)) {

            ElasticsearchPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(clazz);

            if (!entity.hasScoreProperty()) {
                return;
            }

            entity.getPropertyAccessor(result) //
                    .setProperty(entity.getScoreProperty(), score);
        }
    }

    private <T> void populateScriptFields(T result, SearchHit hit) {
        if (hit.getFields() != null && !hit.getFields().isEmpty() && result != null) {
            for (java.lang.reflect.Field field : result.getClass().getDeclaredFields()) {
                ScriptedField scriptedField = field.getAnnotation(ScriptedField.class);
                if (scriptedField != null) {
                    String name = scriptedField.name().isEmpty() ? field.getName() : scriptedField.name();
                    DocumentField searchHitField = hit.getFields().get(name);
                    if (searchHitField != null) {
                        field.setAccessible(true);
                        try {
                            field.set(result, searchHitField.getValue());
                        } catch (IllegalArgumentException e) {
                            throw new ElasticsearchException(
                                    "failed to set scripted field: " + name + " with value: " + searchHitField.getValue(),
                                    e);
                        } catch (IllegalAccessException e) {
                            throw new ElasticsearchException("failed to access scripted field: " + name, e);
                        }
                    }
                }
            }
        }
    }

    // End of the code needed to work-around an incompatible class change in Elasticsearch client libraries
    // which makes spring-data-elasticsearch 3.2 fail.
}
