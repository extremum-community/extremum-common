package com.extremum.common.dao;

import com.extremum.common.models.MongoCommonModel;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryResults;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public abstract class MongoCommonDao<T extends MongoCommonModel> extends BasicDAO<T, ObjectId> {

    private static final String ID = MongoCommonModel.FIELDS.id.name();
    private static final String DELETED = MongoCommonModel.FIELDS.deleted.name();

    public MongoCommonDao(Datastore datastore) {
        super(datastore);
    }

    public List<T> listAll() {
        return createNotDeletedQuery().asList();
    }

    public List<T> listByParameters(Map<String, Object> parameters) {
        if (CollectionUtils.isEmpty(parameters)) {
            return listAll();
        }
        Query<T> q = createNotDeletedQuery();
        FindOptions findOptions = new FindOptions();

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            switch (key) {
                case "limit":
                    String limitStr = String.valueOf(entry.getValue());
                    int limit = Integer.valueOf(limitStr);
                    findOptions.limit(limit);
                    break;
                case "offset":
                    String offsetStr = String.valueOf(entry.getValue());
                    int offset = Integer.valueOf(offsetStr);
                    findOptions.skip(offset);
                    break;
                case "ids":
                    Collection ids = (Collection) entry.getValue();
                    List<ObjectId> objectIds = new ArrayList<>();
                    for (Object id : ids) {
                        objectIds.add(new ObjectId(id.toString()));
                    }
                    q.and(q.criteria(ID).in(objectIds));
                    break;
                default:
                    q.and(q.criteria(key).equal(entry.getValue()));
                    break;
            }
        }
        return q.asList(findOptions);
    }

    public T get(ObjectId id) {
        return getByFieldValueQuery(ID, id).map(QueryResults::get).orElse(null);
    }

    public List<T> listByFieldValue(String fieldName, Object fieldValue) {
        return getByFieldValueQuery(fieldName, fieldValue).map(QueryResults::asList).orElse(null);
    }

    private Optional<Query<T>> getByFieldValueQuery(String fieldName, Object fieldValue) {
        if (fieldName == null || fieldValue == null) {
            return Optional.empty();
        }
        Query<T> query = createNotDeletedQuery().field(fieldName).equal(fieldValue);
        return Optional.of(query);
    }

    public T create(T obj) {
        if (obj != null) {
            super.save(obj);
        }
        return obj;
    }

    public List<T> create(List<T> objects) {
        if (!CollectionUtils.isEmpty(objects)) {
            getDatastore().save(objects);
        }
        return objects;
    }

    public T merge(T obj) {
        if (obj != null) {
            getDatastore().merge(obj);
        }
        return obj;
    }

    // TODO возможно нужно менять еще и modified
    public T delete(ObjectId id) {
        if (id == null) {
            return null;
        }
        Query<T> q = createQuery().field(ID).equal(id);
        UpdateOperations<T> ops = createUpdateOperations().set(DELETED, Boolean.TRUE);

        return getDatastore().findAndModify(q, ops);
    }

    public T getSelectedFieldsById(ObjectId id, String[] fieldNames) {
        if (fieldNames == null || fieldNames.length == 0) {
            return null;
        }
        Query<T> query = createNotDeletedQuery();
        query.and(query.criteria(ID).equal(id));

        for (String fieldName : fieldNames) {
            query.project(fieldName, true);
        }
        return query.get();
    }

    private Query<T> createNotDeletedQuery() {
        Query<T> query = createQuery().disableValidation();
        query.or(
                query.criteria(DELETED).doesNotExist(),
                query.criteria(DELETED).equal(Boolean.FALSE)
        );
        return query;
    }
}
