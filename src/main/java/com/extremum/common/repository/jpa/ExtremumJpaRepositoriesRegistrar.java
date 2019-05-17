package com.extremum.common.repository.jpa;

import com.extremum.starter.properties.JpaProperties;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.jpa.repository.config.JpaRepositoryConfigExtension;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * @author rpuch
 */
@EnableConfigurationProperties(JpaProperties.class)
public class ExtremumJpaRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

   	@Override
   	protected Class<? extends Annotation> getAnnotation() {
   		return EnableExtremumJpaRepositories.class;
   	}

   	@Override
   	protected RepositoryConfigurationExtension getExtension() {
   		return new JpaRepositoryConfigExtension();
   	}

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        AnnotationMetadata wrappedMetadata = new BasePackagesOverride(annotationMetadata,
                EnableExtremumJpaRepositories.class, JpaProperties.REPOSITORY_PACKAGES_PROPERTY);
        super.registerBeanDefinitions(wrappedMetadata, registry);
    }

}
