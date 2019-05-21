package com.extremum.everything.dao;

import com.extremum.common.test.TestWithServices;
import com.extremum.everything.collection.Projection;
import com.extremum.starter.CommonConfiguration;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author rpuch
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CommonConfiguration.class)
class SpringDataUniversalDaoTest extends TestWithServices {
    private static final GenericContainer mongo = new GenericContainer("mongo:3.4-xenial")
            .withExposedPorts(27017);
    private static final GenericContainer redis = new GenericContainer("redis:5.0.4")
            .withExposedPorts(6379);

    static {
        Arrays.asList(mongo, redis).forEach(GenericContainer::start);

        System.setProperty("mongo.uri", "mongodb://" + mongo.getContainerIpAddress()
                + ":" + mongo.getFirstMappedPort());
        System.setProperty("redis.uri", String.format("redis://%s:%d", redis.getContainerIpAddress(), redis.getFirstMappedPort()));
    }

    @Autowired
    private MongoOperations mongoOperations;

    private SpringDataUniversalDao universalDao;

    @BeforeEach
    void setUp() {
        universalDao = new SpringDataUniversalDao(mongoOperations);
    }

    @Test
    void test() {
        House house1 = new House("1");
        House house2 = new House("2a");
        mongoOperations.save(house1);
        mongoOperations.save(house2);

        List<ObjectId> houseIds = Arrays.asList(house1.getId(), house2.getId());
        Street street = new Street("Test lane",
                houseIds.stream().map(Object::toString).collect(Collectors.toList()));
        mongoOperations.save(street);

        List<House> retrievedHouses = universalDao.retrieveByIds(houseIds, House.class, Projection.empty());
        assertThat(retrievedHouses, hasSize(2));
    }

}