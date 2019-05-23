package com.extremum.common.repository.jpa;

import com.extremum.common.dao.PostgresCommonDao;
import com.extremum.common.models.PostgresBasicModel;
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
