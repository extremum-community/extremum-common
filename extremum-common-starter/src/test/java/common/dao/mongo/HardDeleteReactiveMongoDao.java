package common.dao.mongo;

import io.extremum.common.repository.SeesSoftlyDeletedRecords;
import io.extremum.mongo.dao.impl.SpringDataReactiveMongoCommonDao;
import models.HardDeleteMongoModel;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface HardDeleteReactiveMongoDao extends SpringDataReactiveMongoCommonDao<HardDeleteMongoModel> {
    Flux<HardDeleteMongoModel> findByName(String name);

    @SeesSoftlyDeletedRecords
    Flux<HardDeleteMongoModel> findEvenDeletedByName(String name);

    Mono<Long> countByName(String name);

    @SeesSoftlyDeletedRecords
    Mono<Long> countEvenDeletedByName(String name);
}
