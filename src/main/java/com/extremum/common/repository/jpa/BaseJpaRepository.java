package com.extremum.common.repository.jpa;

import com.extremum.common.dao.PostgresCommonDao;
import com.extremum.common.models.PersistableCommonModel;
import com.extremum.common.models.PostgresCommonModel;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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

    private <S> NotDeleted<S> notDeleted() {
        return new NotDeleted<>();
    }

    @Override
    public List<T> findAll(Sort sort) {
        return getQuery(notDeleted(), sort).getResultList();
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        if (pageable.isUnpaged()) {
            return new PageImpl<>(findAll());
        }

        return findAll(notDeleted(), pageable);
    }

    @Override
    public boolean existsById(UUID uuid) {
        return findById(uuid).isPresent();
    }

    @Override
    public Optional<T> findById(UUID id) {
        Specification<T> specification = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(ID), id);
        return findOne(specification.and(notDeleted()));
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
    public <S extends T> List<S> findAll(Example<S> example) {
        return getQuery(new ExampleSpecification<>(example).and(notDeleted()),
                example.getProbeType(), Sort.unsorted()).getResultList();
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        return getQuery(new ExampleSpecification<>(example).and(notDeleted()),
                example.getProbeType(), sort).getResultList();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        Optional<T> optionalEntity = findById(id);
        if (optionalEntity.isPresent()) {
            T entity = optionalEntity.get();
            entity.setDeleted(true);
            entityManager.merge(entity);
        }
    }

    @Override
    public long count() {
        return super.count(notDeleted());
    }

    @Override
    public <S extends T> Optional<S> findOne(Example<S> example) {
        try {
            return Optional.of(
                    getQuery(new ExampleSpecification<>(example).and(notDeleted()), example.getProbeType(),
                            Sort.unsorted()).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        Specification<S> spec = new ExampleSpecification<>(example).and(notDeleted());
        Class<S> probeType = example.getProbeType();
        TypedQuery<S> query = getQuery(spec, probeType, pageable);

        return pageable.isUnpaged() ? new PageImpl<>(query.getResultList())
                : readPage(query, probeType, pageable, spec);
    }

    private static class NotDeleted<T> implements Specification<T> {
        @Override
        public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
            return builder.isFalse(root.get(PersistableCommonModel.FIELDS.deleted.name()));
        }
    }

    @Override
    public <S extends T> long count(Example<S> example) {
        Specification<S> spec = new ExampleSpecification<>(example).and(notDeleted());
        return executeCountQuery(getCountQuery(spec, example.getProbeType()));
    }

    private static long executeCountQuery(TypedQuery<Long> query) {

   		Assert.notNull(query, "TypedQuery must not be null!");

   		List<Long> totals = query.getResultList();
   		long total = 0L;

   		for (Long element : totals) {
   			total += element == null ? 0 : element;
   		}

   		return total;
   	}

    @Override
    public <S extends T> boolean exists(Example<S> example) {
        Specification<S> spec = new ExampleSpecification<>(example).and(notDeleted());
        return !getQuery(spec, example.getProbeType(), Sort.unsorted()).getResultList().isEmpty();
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

    private static class ExampleSpecification<S> implements Specification<S> {

   		private static final long serialVersionUID = 1L;

   		private final Example<S> example;

   		ExampleSpecification(Example<S> example) {

   			Assert.notNull(example, "Example must not be null!");
   			this.example = example;
   		}

   		/*
   		 * (non-Javadoc)
   		 * @see org.springframework.data.jpa.domain.Specification#toPredicate(javax.persistence.criteria.Root, javax.persistence.criteria.CriteriaQuery, javax.persistence.criteria.CriteriaBuilder)
   		 */
   		@Override
   		public Predicate toPredicate(Root<S> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
   			return QueryByExamplePredicateBuilder.getPredicate(root, cb, example);
   		}
   	}
}
