package com.extremum.repositories;

import com.extremum.common.repository.common.ExpressionsSupportingRepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.jpa.repository.config.JpaRepositoryConfigExtension;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * @author rpuch
 */
public class ExtremumJpaRepositoriesRegistrar extends ExpressionsSupportingRepositoryBeanDefinitionRegistrarSupport {

   	@Override
   	protected Class<? extends Annotation> getAnnotation() {
   		return EnableExtremumJpaRepositories.class;
   	}

   	@Override
   	protected RepositoryConfigurationExtension getExtension() {
   		return new JpaRepositoryConfigExtension();
   	}

}
