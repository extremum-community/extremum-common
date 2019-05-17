package com.extremum.common.repository.mongo;

import com.extremum.common.repository.BasePackagesOverride;
import com.extremum.starter.properties.MongoProperties;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.mongodb.repository.config.MongoRepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * @author rpuch
 */
public class ExtremumMongoRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

   	@Override
   	protected Class<? extends Annotation> getAnnotation() {
   		return EnableExtremumMongoRepositories.class;
   	}

   	@Override
   	protected RepositoryConfigurationExtension getExtension() {
   		return new MongoRepositoryConfigurationExtension();
   	}

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        AnnotationMetadata wrappedMetadata = new BasePackagesOverride(annotationMetadata,
                EnableExtremumMongoRepositories.class, MongoProperties.REPOSITORY_PACKAGES_PROPERTY);
        super.registerBeanDefinitions(wrappedMetadata, registry);
    }

}
