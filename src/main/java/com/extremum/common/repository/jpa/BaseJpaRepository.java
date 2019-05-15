package com.extremum.common.repository.jpa;

import com.extremum.common.dao.PostgresCommonDao;
import com.extremum.common.models.PersistableCommonModel;
import com.extremum.common.models.PostgresCommonModel;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.*;

/**
 * @author rpuch
 */
public class BaseJpaRepository<T extends PostgresCommonModel> extends SimpleJpaRepository<T, UUID>
        implements PostgresCommonDao<T> {
    private final JpaEntityInformation<T, ?> entityInformation;
    private final EntityManager entityManager;

    private static final String ID = PersistableCommonModel.FIELDS.id.name();

    public BaseJpaRepository(JpaEntityInformation<T, ?> entityInformation,
            EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityInformation = entityInformation;
        this.entityManager = entityManager;
    }

    @Override
    public List<T> findAll() {
        return getQuery(notDeleted(), Sort.unsorted()).getResultList();
    }

    private NotDeleted<T> notDeleted() {
        return new NotDeleted<>();
    }

    @Override
    public List<T> findAll(Sort sort) {
        return getQuery(notDeleted(), sort).getResultList();
    }

    @Override
    public Optional<T> findById(UUID id) {
        Specification<T> specification = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(ID), id);
        return findOne(notDeleted().and(specification));
    }

    @Override
    public List<T> findAllById(Iterable<UUID> ids) {
        Assert.notNull(ids, "The given Iterable of Id's must not be null!");

        if (!ids.iterator().hasNext()) {
            return Collections.emptyList();
        }

        if (entityInformation.hasCompositeId()) {

            List<T> results = new ArrayList<>();

            for (UUID id : ids) {
                findById(id).ifPresent(results::add);
            }

            return results;
        }

        ByIdsSpecification<T> specification = new ByIdsSpecification<>(entityInformation);
        TypedQuery<T> query = getQuery(specification.and(notDeleted()), Sort.unsorted());

        return query.setParameter(specification.parameter, ids).getResultList();
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

    private static final class ByIdsSpecification<T> implements Specification<T> {

   		private static final long serialVersionUID = 1L;

   		private final JpaEntityInformation<T, ?> entityInformation;

   		@Nullable
           ParameterExpression<Iterable> parameter;

   		ByIdsSpecification(JpaEntityInformation<T, ?> entityInformation) {
   			this.entityInformation = entityInformation;
   		}

   		/*
   		 * (non-Javadoc)
   		 * @see org.springframework.data.jpa.domain.Specification#toPredicate(javax.persistence.criteria.Root, javax.persistence.criteria.CriteriaQuery, javax.persistence.criteria.CriteriaBuilder)
   		 */
   		public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

   			Path<?> path = root.get(entityInformation.getIdAttribute());
   			parameter = cb.parameter(Iterable.class);
   			return path.in(parameter);
   		}
   	}
}
