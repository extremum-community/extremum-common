package com.extremum.repositories;

import com.extremum.dao.PostgresCommonDao;
import com.extremum.models.PostgresBasicModel;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;

import javax.persistence.EntityManager;

/**
 * @author rpuch
 */
public class HardDeleteJpaRepository<T extends PostgresBasicModel> extends BaseJpaRepository<T>
        implements PostgresCommonDao<T> {
    public HardDeleteJpaRepository(
            JpaEntityInformation<T, ?> entityInformation,
            EntityManager entityManager) {
        super(entityInformation, entityManager);
    }
}
