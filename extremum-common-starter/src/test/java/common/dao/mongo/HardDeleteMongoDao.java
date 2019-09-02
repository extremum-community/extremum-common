package common.dao.mongo;

import io.extremum.mongo.dao.impl.SpringDataMongoCommonDao;
import io.extremum.common.repository.SeesSoftlyDeletedRecords;
import models.HardDeleteMongoModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HardDeleteMongoDao extends SpringDataMongoCommonDao<HardDeleteMongoModel> {
    List<HardDeleteMongoModel> findByName(String name);

    @SeesSoftlyDeletedRecords
    List<HardDeleteMongoModel> findEvenDeletedByName(String name);

    long countByName(String name);

    @SeesSoftlyDeletedRecords
    long countEvenDeletedByName(String name);
}
