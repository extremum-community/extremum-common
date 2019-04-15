package com.extremum.common.descriptor.config;

import com.extremum.common.descriptor.dao.DescriptorDao;
import com.extremum.common.descriptor.dao.impl.BaseDescriptorDaoImpl;
import com.extremum.common.descriptor.serde.mongo.DescriptorConverter;
import com.extremum.common.descriptor.serde.mongo.DescriptorCreator;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties({RedisProperties.class, MongoProperties.class, DescriptorsProperties.class})
public class DescriptorConfiguration {

    @Autowired
    private RedisProperties redisProps;
    @Autowired
    private MongoProperties mongoProps;
    @Autowired
    private DescriptorsProperties descriptorsProperties;

    @Bean
    public RedissonClient redissonClient() {
        Config config =  new Config();
        config.useSingleServer().setAddress(redisProps.getUri());
        return Redisson.create(config);
    }

    @Bean
    public Datastore descriptorsStore() {
        Morphia morphia = new Morphia();
        morphia.getMapper().getOptions().setStoreEmpties(true);
        morphia.getMapper().getOptions().setObjectFactory(new DescriptorCreator());
        morphia.getMapper().getConverters().addConverter(DescriptorConverter.class);
        MongoClientURI databaseUri = new MongoClientURI(mongoProps.getUri());
        MongoClient mongoClient = new MongoClient(databaseUri);
        Datastore datastore = morphia.createDatastore(mongoClient, mongoProps.getDbName());

        datastore.ensureIndexes();

        return datastore;
    }

    @Bean
    public DescriptorDao descriptorDao(RedissonClient redissonClient, Datastore descriptorsStore) {
        return new BaseDescriptorDaoImpl(redissonClient, descriptorsStore, descriptorsProperties.getDescriptorsMapName(),
                descriptorsProperties.getInternalIdsMapName(), redisProps.getCacheSize(), redisProps.getIdleTime());
    }
}
