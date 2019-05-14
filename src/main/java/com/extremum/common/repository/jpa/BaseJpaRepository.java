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
public class BaseJpaRepository<T extends PostgresCommonModel> extends SimpleJpaRepository<T, UUID>
        implements PostgresCommonDao<T> {
    public BaseJpaRepository(JpaEntityInformation<T, ?> entityInformation,
            EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    @Override
    public boolean softDeleteById(UUID id) {
        throw new UnsupportedOperationException();
    }
}
