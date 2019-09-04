package common.dao.mongo;

import io.extremum.common.repository.SeesSoftlyDeletedRecords;
import io.extremum.mongo.dao.impl.SpringDataReactiveMongoCommonDao;
import models.TestMongoModel;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TestReactiveMongoModelDao extends SpringDataReactiveMongoCommonDao<TestMongoModel> {
    Mono<TestMongoModel> findOneByName(String name);

    Flux<TestMongoModel> findAllByName(String name);

    @SeesSoftlyDeletedRecords
    Flux<TestMongoModel> findEvenDeletedByName(String name);

    Mono<Long> countByName(String name);

    @SeesSoftlyDeletedRecords
    Mono<Long> countEvenDeletedByName(String name);
}
