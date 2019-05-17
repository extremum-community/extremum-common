package common.dao.jpa;

import com.extremum.common.repository.jpa.ExtremumJpaRepositoryFactoryBean;
import config.DescriptorConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author rpuch
 */
@Configuration
@Import(DescriptorConfiguration.class)
@EnableJpaRepositories(repositoryFactoryBeanClass = ExtremumJpaRepositoryFactoryBean.class)
public class JpaCommonDaoConfiguration {
}
