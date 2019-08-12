package com.extremum.jpa.repositories;

import com.extremum.common.exceptions.ModelNotFoundException;
import com.extremum.jpa.dao.PostgresCommonDao;
import com.extremum.jpa.models.PostgresBasicModel;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.UUID;

/**
 * @author rpuch
 */
public class HardDeleteJpaRepository<T extends PostgresBasicModel> extends BaseJpaRepository<T>
        implements PostgresCommonDao<T> {
    private final JpaEntityInformation<T, ?> entityInformation;

    public HardDeleteJpaRepository(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);

        this.entityInformation = entityInformation;
    }

    @Override
    @Transactional
    public T deleteByIdAndReturn(UUID id) {
        T entity = findById(id)
                .orElseThrow(() -> new ModelNotFoundException(entityInformation.getJavaType(), id.toString()));
        delete(entity);
        return entity;
    }
}
