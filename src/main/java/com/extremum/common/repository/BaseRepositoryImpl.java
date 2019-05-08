package com.extremum.common.repository;

import com.extremum.common.dao.MongoCommonDao;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.PersistableCommonModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author rpuch
 */
public class BaseRepositoryImpl<T extends MongoCommonModel> extends SimpleMongoRepository<T, ObjectId>
        implements MongoCommonDao<T> {
    private static final String DELETED = PersistableCommonModel.FIELDS.deleted.name();

    private final MongoEntityInformation<T, ObjectId> entityInformation;
    private final MongoOperations mongoOperations;

    public BaseRepositoryImpl(MongoEntityInformation<T, ObjectId> metadata,
            MongoOperations mongoOperations) {
        super(metadata, mongoOperations);

        this.entityInformation = metadata;
        this.mongoOperations = mongoOperations;
    }

    @Override
    public Optional<T> findById(ObjectId id) {
        Assert.notNull(id, "The given id must not be null!");

        Query query = queryForNotDeletedAnd(where("_id").is(id));

        return findOneByQuery(query);
    }

    @Override
    public boolean isDeleted(ObjectId objectId) {
        return !existsById(objectId);
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
        return new Criteria().orOperator(
                where(DELETED).exists(false),
                where(DELETED).is(false)
        );
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
    public boolean softDeleteById(ObjectId id) {
        Query query = new Query(where("_id").is(id));
        Update update = new Update();
        update.set(DELETED, true);
        T modified = mongoOperations.findAndModify(query, update, entityInformation.getJavaType());
        return modified != null && modified.getDeleted();
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
                case "limit":
                    String limitStr = String.valueOf(entry.getValue());
                    int limit = Integer.valueOf(limitStr);
                    optionalLimit = OptionalInt.of(limit);
                    break;
                case "offset":
                    String offsetStr = String.valueOf(entry.getValue());
                    int offset = Integer.valueOf(offsetStr);
                    optionalOffset = OptionalInt.of(offset);
                    break;
                case "ids":
                    Collection ids = (Collection) entry.getValue();
                    List<ObjectId> objectIds = new ArrayList<>();
                    for (Object id : ids) {
                        objectIds.add(new ObjectId(id.toString()));
                    }
                    leafCriteria.add(where("_id").in(objectIds));
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

        Query query = queryForNotDeletedAnd(where("_id").is(id));
        Arrays.stream(fieldNames).forEach(fieldName -> query.fields().include(fieldName));

        return findOneByQuery(query);
    }
}
