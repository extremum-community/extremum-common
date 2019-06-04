package com.extremum.common.repository.mongo;

import com.extremum.common.dao.MongoCommonDao;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.PersistableCommonModel;
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
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Differs from the standard {@link SimpleMongoRepository} in two aspects:
 * 1. has implementations for our extension methods
 * 2. implements soft-deletion logic; that is, all deletions are replaced with setting 'deleted' flag to true,
 * and all find operations filter out documents with 'deleted' set to true.
 *
 * @author rpuch
 */
public class SoftDeleteMongoRepository<T extends MongoCommonModel> extends BaseMongoRepository<T>
        implements MongoCommonDao<T> {
    private static final String ID = "_id";
    private static final String DELETED = PersistableCommonModel.FIELDS.deleted.name();

    private final MongoEntityInformation<T, ObjectId> entityInformation;
    private final MongoOperations mongoOperations;
    private final SoftDeletion softDeletion = new SoftDeletion();

    public SoftDeleteMongoRepository(MongoEntityInformation<T, ObjectId> metadata,
            MongoOperations mongoOperations) {
        super(metadata, mongoOperations);

        this.entityInformation = metadata;
        this.mongoOperations = mongoOperations;
    }

    @Override
    Query notDeletedQueryWith(Criteria criteria) {
        Criteria finalCriteria = new Criteria().andOperator(
                notDeleted(),
                criteria
        );
        return new Query(finalCriteria);
    }

    @Override
    Query notDeletedQuery() {
        return new Query(notDeleted());
    }

    @Override
    public Optional<T> findById(ObjectId id) {
        Assert.notNull(id, "The given id must not be null!");

        Query query = notDeletedQueryWith(getIdCriteria(id));

        return findOneByQuery(query);
    }

    @Override
    public Iterable<T> findAllById(Iterable<ObjectId> ids) {
        Criteria inCriteria = new Criteria(entityInformation.getIdAttribute())
                .in(Streamable.of(ids).stream().collect(StreamUtils.toUnmodifiableList()));
        return findAllByQuery(notDeletedQueryWith(inCriteria));
    }

    @Override
    public long count() {
        return mongoOperations.count(notDeletedQuery(), entityInformation.getCollectionName());
    }

    @Override
    public <S extends T> long count(Example<S> example) {
        Assert.notNull(example, "Sample must not be null!");

        Query q = notDeletedQueryWith(new Criteria().alike(example));
        return mongoOperations.count(q, example.getProbeType(), entityInformation.getCollectionName());
    }

    private Criteria notDeleted() {
        return softDeletion.notDeleted();
    }

    @Override
    public List<T> findAll() {
        return findAllByQuery(notDeletedQuery());
    }

    @Override
    public List<T> findAll(Sort sort) {
        return findAllByQuery(notDeletedQuery().with(sort));
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
        return notDeletedQueryWith(new Criteria().alike(example));
    }

    @Override
    public boolean existsById(ObjectId id) {
        Assert.notNull(id, "The given id must not be null!");

        return mongoOperations.exists(notDeletedQueryWith(where(entityInformation.getIdAttribute()).is(id)),
                entityInformation.getJavaType(), entityInformation.getCollectionName());
    }

    @Override
    public void deleteById(ObjectId id) {
        Query query = new Query(where(ID).is(id));
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
    public void deleteAll(Iterable<? extends T> entities) {
        Assert.notNull(entities, "The given Iterable of entities must not be null!");

        entities.forEach(this::delete);
    }
}