package com.extremum.common.repository.jpa;

import com.extremum.common.dao.PostgresCommonDao;
import com.extremum.common.models.PostgresCommonModel;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import java.util.UUID;

/**
 * @author rpuch
 */
public class HardDeleteJpaRepository<T extends PostgresCommonModel> extends SimpleJpaRepository<T, UUID>
        implements PostgresCommonDao<T> {
    public HardDeleteJpaRepository(
            JpaEntityInformation<T, ?> entityInformation,
            EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("We don't allow to delete all the records in one go");
    }

    @Override
    public void deleteAllInBatch() {
        throw new UnsupportedOperationException("We don't allow to delete all the records in one go");
    }
}
