package io.extremum.common.secondaryds;

import io.extremum.mongo.model.MongoCommonModel;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelOnSecondaryDatasourceFilterTest {
    private final ModelOnSecondaryDatasourceFilter filter = new ModelOnSecondaryDatasourceFilter();
    private final MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();

    @Test
    void shouldMatchNonReactiveRepositoryWithModelAnnotatedAsSecondaryDatasource() throws Exception {
        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(
                SecondaryDSModelRepository.class.getName());

        assertTrue(filter.match(metadataReader, metadataReaderFactory));
    }

    @Test
    void shouldMatchReactiveRepositoryWithModelAnnotatedAsSecondaryDatasource() throws Exception {
        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(
                ReactiveSecondaryDSModelRepository.class.getName());

        assertTrue(filter.match(metadataReader, metadataReaderFactory));
    }

    @Test
    void shouldNotMatchNonRepositoryClass() throws Exception {
        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(String.class.getName());

        assertFalse(filter.match(metadataReader, metadataReaderFactory));
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @SecondaryDatasource
    public @interface TestDatasource {}

    @TestDatasource
    private static class SecondaryDSModel extends MongoCommonModel {
    }

    private interface SecondaryDSModelRepository extends MongoRepository<ObjectId, SecondaryDSModel> {
    }

    private interface ReactiveSecondaryDSModelRepository extends ReactiveMongoRepository<ObjectId, SecondaryDSModel> {
    }
}