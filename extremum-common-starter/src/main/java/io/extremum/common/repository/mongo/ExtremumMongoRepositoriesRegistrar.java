package io.extremum.common.repository.mongo;

import io.extremum.common.repository.common.ExpressionsSupportingRepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.mongodb.repository.config.MongoRepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * @author rpuch
 */
public class ExtremumMongoRepositoriesRegistrar extends ExpressionsSupportingRepositoryBeanDefinitionRegistrarSupport {

   	@Override
   	protected Class<? extends Annotation> getAnnotation() {
   		return EnableExtremumMongoRepositories.class;
   	}

   	@Override
   	protected RepositoryConfigurationExtension getExtension() {
   		return new MongoRepositoryConfigurationExtension();
   	}

}
