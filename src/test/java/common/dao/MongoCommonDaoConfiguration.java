package common.dao;

import com.extremum.common.repository.BaseRepositoryImpl;
import com.extremum.starter.CommonConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * @author rpuch
 */
@Configuration
@Import(CommonConfiguration.class)
@EnableMongoRepositories(repositoryBaseClass = BaseRepositoryImpl.class)
public class MongoCommonDaoConfiguration {
}
