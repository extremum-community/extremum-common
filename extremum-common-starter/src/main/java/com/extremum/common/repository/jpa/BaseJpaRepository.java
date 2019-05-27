package com.extremum.common.repository.jpa;

import com.extremum.common.dao.PostgresCommonDao;
import com.extremum.common.models.PostgresBasicModel;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import java.util.UUID;

/**
 * @author rpuch
 */
abstract class BaseJpaRepository<T extends PostgresBasicModel> extends SimpleJpaRepository<T, UUID>
        implements PostgresCommonDao<T> {
    BaseJpaRepository(
            JpaEntityInformation<T, ?> entityInformation,
            EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    @Override
    public final void deleteAll() {
        throw new UnsupportedOperationException("We don't allow to delete all the records in one go");
    }

    @Override
    public final void deleteAllInBatch() {
        throw new UnsupportedOperationException("We don't allow to delete all the records in one go");
    }
}
