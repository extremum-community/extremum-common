package com.extremum.common.repository.mongo;

import com.extremum.common.dao.MongoCommonDao;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.PersistableCommonModel;
import com.extremum.common.models.QueryFields;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.data.util.StreamUtils;
import org.springframework.data.util.Streamable;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Differs from the standard {@link SimpleMongoRepository} in two aspects:
 * 1. has implementations for our extension methods
 * 2. implements soft-deletion logic; that is, all deletions are replached with setting 'deleted' flag to true,
 * and all find operations filter out documents with 'deleted' set to true.
 *
 * @author rpuch
 */
public class BaseMongoRepository<T extends MongoCommonModel> extends SimpleMongoRepository<T, ObjectId>
        implements MongoCommonDao<T> {
    private static final String DELETED = PersistableCommonModel.FIELDS.deleted.name();

    private final MongoEntityInformation<T, ObjectId> entityInformation;
    private final MongoOperations mongoOperations;
    private final SoftDeletion softDeletion = new SoftDeletion();

    public BaseMongoRepository(MongoEntityInformation<T, ObjectId> metadata,
            MongoOperations mongoOperations) {
        super(metadata, mongoOperations);

        this.entityInformation = metadata;
        this.mongoOperations = mongoOperations;
    }

    @Override
    public Optional<T> findById(ObjectId id) {
        Assert.notNull(id, "The given id must not be null!");

        Query query = queryForNotDeletedAnd(getIdCriteria(id));

        return findOneByQuery(query);
    }

    private Criteria getIdCriteria(Object id) {
        return where(entityInformation.getIdAttribute()).is(id);
    }

    @Override
    public Iterable<T> findAllById(Iterable<ObjectId> ids) {
        Criteria inCriteria = new Criteria(entityInformation.getIdAttribute())
                .in(Streamable.of(ids).stream().collect(StreamUtils.toUnmodifiableList()));
        return findAll(queryForNotDeletedAnd(inCriteria));
    }

    @Override
    public long count() {
        return mongoOperations.count(new Query(notDeleted()), entityInformation.getCollectionName());
    }

    @Override
    public <S extends T> long count(Example<S> example) {
        Assert.notNull(example, "Sample must not be null!");

        Query q = queryForNotDeletedAndAlike(example);
        return mongoOperations.count(q, example.getProbeType(), entityInformation.getCollectionName());
    }

    private Optional<T> findOneByQuery(Query query) {
        T result = mongoOperations.findOne(query,
                entityInformation.getJavaType(), entityInformation.getCollectionName());
        return Optional.ofNullable(result);
    }

    private Query queryForNotDeletedAnd(Criteria criteria) {
        Criteria finalCriteria = new Criteria().andOperator(
                notDeleted(),
                criteria
        );
        return new Query(finalCriteria);
    }

    private Criteria notDeleted() {
        return softDeletion.notDeleted();
    }

    @Override
    public List<T> findAll() {
        return findAll(new Query(notDeleted()));
    }

    private List<T> findAll(@Nullable Query query) {
   		if (query == null) {
   			return Collections.emptyList();
   		}

   		return mongoOperations.find(query, entityInformation.getJavaType(), entityInformation.getCollectionName());
   	}

    @Override
    public List<T> findAll(Sort sort) {
        return findAll(new Query(notDeleted()).with(sort));
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        Assert.notNull(example, "Sample must not be null!");
        Assert.notNull(sort, "Sort must not be null!");

        Query q = queryForNotDeletedAndAlike(example).with(sort);

        return mongoOperations.find(q, example.getProbeType(), entityInformation.getCollectionName());
    }

    @Override
    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        Assert.notNull(example, "Sample must not be null!");
        Assert.notNull(pageable, "Pageable must not be null!");

        Query q = queryForNotDeletedAndAlike(example).with(pageable);
        List<S> list = mongoOperations.find(q, example.getProbeType(), entityInformation.getCollectionName());

        return PageableExecutionUtils.getPage(list, pageable,
                () -> mongoOperations.count(q, example.getProbeType(), entityInformation.getCollectionName()));
    }

    @Override
    public <S extends T> Optional<S> findOne(Example<S> example) {
        Assert.notNull(example, "Sample must not be null!");

        Query q = queryForNotDeletedAndAlike(example);
        return Optional
                .ofNullable(mongoOperations.findOne(q, example.getProbeType(), entityInformation.getCollectionName()));
    }

    @Override
    public <S extends T> boolean exists(Example<S> example) {
        Assert.notNull(example, "Sample must not be null!");

        Query q = queryForNotDeletedAndAlike(example);
        return mongoOperations.exists(q, example.getProbeType(), entityInformation.getCollectionName());
    }

    private <S extends T> Query queryForNotDeletedAndAlike(Example<S> example) {
        return queryForNotDeletedAnd(new Criteria().alike(example));
    }

    @Override
    public boolean existsById(ObjectId id) {
        Assert.notNull(id, "The given id must not be null!");

        return mongoOperations.exists(queryForNotDeletedAnd(where(entityInformation.getIdAttribute()).is(id)),
                entityInformation.getJavaType(), entityInformation.getCollectionName());
    }

    @Override
    public void deleteById(ObjectId id) {
        Query query = new Query(getIdCriteria(id));
        Update update = updateDeletedToTrue();
        mongoOperations.findAndModify(query, update, entityInformation.getJavaType());
    }

    private Update updateDeletedToTrue() {
        Update update = new Update();
        update.set(DELETED, true);
        return update;
    }

    @Override
    public void delete(T entity) {
        entity.setDeleted(true);
        save(entity);
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("We don't allow to delete all the records in one go");
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");

        entities.forEach(this::delete);
    }

    @Override
    public List<T> listByParameters(Map<String, Object> parameters) {
        if (CollectionUtils.isEmpty(parameters)) {
            return findAll();
        }

        OptionalInt optionalLimit = OptionalInt.empty();
        OptionalInt optionalOffset = OptionalInt.empty();
        final List<Criteria> leafCriteria = new ArrayList<>();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            switch (key) {
                case QueryFields.LIMIT:
                    String limitStr = String.valueOf(entry.getValue());
                    int limit = Integer.valueOf(limitStr);
                    optionalLimit = OptionalInt.of(limit);
                    break;
                case QueryFields.OFFSET:
                    String offsetStr = String.valueOf(entry.getValue());
                    int offset = Integer.valueOf(offsetStr);
                    optionalOffset = OptionalInt.of(offset);
                    break;
                case QueryFields.IDS:
                    Collection ids = (Collection) entry.getValue();
                    List<ObjectId> objectIds = new ArrayList<>();
                    for (Object id : ids) {
                        objectIds.add(new ObjectId(id.toString()));
                    }
                    leafCriteria.add(where(entityInformation.getIdAttribute()).in(objectIds));
                    break;
                default:
                    leafCriteria.add(where(key).is(entry.getValue()));
                    break;
            }
        }

        final Query query;
        if (leafCriteria.isEmpty()) {
            query = new Query(notDeleted());
        } else {
            query = queryForNotDeletedAnd(new Criteria().andOperator(leafCriteria.toArray(new Criteria[0])));
        }

        optionalOffset.ifPresent(query::skip);
        optionalLimit.ifPresent(query::limit);

        return findAll(query);
    }

    @Override
    public List<T> listByFieldValue(String fieldName, Object fieldValue) {
        return findAll(queryForNotDeletedAnd(where(fieldName).is(fieldValue)));
    }

    @Override
    public Optional<T> getSelectedFieldsById(ObjectId id, String... fieldNames) {
        Assert.notNull(id, "The given id must not be null!");

        Query query = queryForNotDeletedAnd(getIdCriteria(id));
        Arrays.stream(fieldNames).forEach(fieldName -> query.fields().include(fieldName));

        return findOneByQuery(query);
    }
}
