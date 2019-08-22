package io.extremum.everything.dao;

import io.extremum.everything.TestWithServices;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.starter.CommonConfiguration;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * @author rpuch
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CommonConfiguration.class)
class SpringDataUniversalDaoTest extends TestWithServices {
    @Autowired
    private MongoOperations mongoOperations;
    @Autowired
    private ReactiveMongoOperations reactiveMongoOperations;

    private SpringDataUniversalDao universalDao;

    private List<ObjectId> houseIds;

    @BeforeEach
    void setUp() {
        universalDao = new SpringDataUniversalDao(mongoOperations, reactiveMongoOperations);

        House house1 = new House("1");
        House house2 = new House("2a");
        mongoOperations.save(house1);
        mongoOperations.save(house2);

        houseIds = Arrays.asList(house1.getId(), house2.getId());
    }

    @Test
    void givenTwoHousesExist_whenRetrievingByTheirIdsWithEmptyProjection_then2HousesShouldBeReturned() {
        CollectionFragment<House> retrievedHouses = universalDao.retrieveByIds(houseIds,
                House.class, Projection.empty());

        assertThat(retrievedHouses.elements(), hasSize(2));
        assertThat(retrievedHouses.total().orElse(1000), is(2L));
    }

    @Test
    void givenTwoHousesExist_whenRetrievingByTheirIdsWithOffset1_then1HouseShouldBeReturnedButTotalShouldBe2() {
        CollectionFragment<House> retrievedHouses = universalDao.retrieveByIds(houseIds,
                House.class, Projection.offsetLimit(1, 10));
        
        assertThat(retrievedHouses.elements(), hasSize(1));
        assertThat(retrievedHouses.total().orElse(1000), is(2L));
    }

    @Test
    void givenTwoHousesExist_whenStreamingByTheirIdsWithEmptyProjection_then2HousesShouldBeReturned() {
        Flux<House> retrievedHouses = universalDao.streamByIds(houseIds,
                House.class, Projection.empty());

        assertThat(retrievedHouses.toStream().collect(Collectors.toList()), hasSize(2));
    }

    @Test
    void givenTwoHousesExist_whenStreamingByTheirIdsWithOffset1_then1HouseShouldBeReturnedButTotalShouldBe2() {
        Flux<House> retrievedHouses = universalDao.streamByIds(houseIds,
                House.class, Projection.offsetLimit(1, 10));

        assertThat(retrievedHouses.toStream().collect(Collectors.toList()), hasSize(1));
    }

}