package common.dao.mongo;

import com.extremum.common.repository.BaseMongoRepository;
import com.extremum.common.repository.SoftDeleteMongoRepositoryFactoryBean;
import config.DescriptorConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * @author rpuch
 */
@Configuration
@Import(DescriptorConfiguration.class)
@EnableMongoRepositories(repositoryBaseClass = BaseMongoRepository.class,
        repositoryFactoryBeanClass = SoftDeleteMongoRepositoryFactoryBean.class)
public class MongoCommonDaoConfiguration {
}
