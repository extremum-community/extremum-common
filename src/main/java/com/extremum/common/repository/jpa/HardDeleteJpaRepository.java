package com.extremum.common.repository.jpa;

import com.extremum.common.dao.PostgresCommonDao;
import com.extremum.common.models.PostgresCommonModel;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;

import javax.persistence.EntityManager;

/**
 * @author rpuch
 */
public class HardDeleteJpaRepository<T extends PostgresCommonModel> extends BaseJpaRepository<T>
        implements PostgresCommonDao<T> {
    public HardDeleteJpaRepository(
            JpaEntityInformation<T, ?> entityInformation,
            EntityManager entityManager) {
        super(entityInformation, entityManager);
    }
}
