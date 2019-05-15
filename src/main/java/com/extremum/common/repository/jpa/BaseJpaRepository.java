package com.extremum.common.repository.jpa;

import com.extremum.common.dao.PostgresCommonDao;
import com.extremum.common.models.PersistableCommonModel;
import com.extremum.common.models.PostgresCommonModel;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author rpuch
 */
public class BaseJpaRepository<T extends PostgresCommonModel> extends SimpleJpaRepository<T, UUID>
        implements PostgresCommonDao<T> {
    private final EntityManager entityManager;

    private static final String ID = PersistableCommonModel.FIELDS.id.name();

    public BaseJpaRepository(JpaEntityInformation<T, ?> entityInformation,
            EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    public List<T> findAll() {
        return getQuery(new NotDeleted<>(), Sort.unsorted()).getResultList();
    }

    @Override
    public Optional<T> findById(UUID id) {
        Specification<T> specification = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(ID), id);
        return findOne(new NotDeleted<T>().and(specification));
    }

    @Override
    @Transactional
    public boolean softDeleteById(UUID id) {
        Optional<T> optionalEntity = findById(id);
        if (optionalEntity.isPresent()) {
            T entity = optionalEntity.get();
            entity.setDeleted(true);
            entityManager.merge(entity);
            return true;
        } else {
            return false;
        }
    }

    private static class NotDeleted<T extends PostgresCommonModel> implements Specification<T> {
        @Override
        public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
            return builder.isFalse(root.get(PersistableCommonModel.FIELDS.deleted.name()));
        }
    }
}
