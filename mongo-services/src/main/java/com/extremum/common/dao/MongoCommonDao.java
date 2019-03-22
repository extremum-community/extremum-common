package com.extremum.common.dao;

import com.extremum.common.models.MongoCommonModel;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.*;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nullable;
import java.time.ZonedDateTime;
import java.util.*;


public abstract class MongoCommonDao<T extends MongoCommonModel> extends BasicDAO<T, ObjectId> {

    public MongoCommonDao(Datastore datastore) {
        super(datastore);
    }

    public List<T> listAll() {
        Query<T> q = createQuery().disableValidation();
        orDeleted(q);
        return q.asList();
    }

    public List<T> listByParameters(Map<String, Object> parameters) {
        if (CollectionUtils.isEmpty(parameters)) {
            return this.listAll();
        }

        Query<T> q = createQuery().disableValidation();
        List<Criteria> arr = new ArrayList<>();
        arr.add(orDeleted(q));

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            switch (key) {
                case "limit":
                    String limitStr = (String) entry.getValue();
                    Integer limit = Integer.valueOf(limitStr);
                    q.limit(limit);
                    break;
                case "offset":
                    String offsetStr = (String) entry.getValue();
                    Integer offset = Integer.valueOf(offsetStr);
                    q.offset(offset);
                    break;
                case "ids":
                    Collection ids = (Collection) entry.getValue();
                    List<ObjectId> objectIds = new ArrayList<>();
                    for (Object id : ids) {
                        ObjectId objectId = new ObjectId(id.toString());
                        objectIds.add(objectId);
                    }
                    arr.add(q.criteria("id").in(objectIds));
                    break;
                default:
                    arr.add(q.criteria(key).equal(entry.getValue()));
                    break;
            }
        }

        if (CollectionUtils.isEmpty(arr)) {
            return q.asList();
        }

        q.and(arr.toArray(new Criteria[arr.size()]));

        return q.asList();
    }

    public T get (ObjectId id) {
        if (id == null) {
            return null;
        }
        Query<T> q = createQuery().disableValidation();
        orDeleted(q);

        return q.field(MongoCommonModel.FIELDS.id.name()).equal(id).get();
    }

    public List<T> getByFieldValue (String fieldName, Object fieldValue) {
        if (fieldName == null || fieldValue == null) {
            return null;
        }
        Query<T> q = createQuery().disableValidation();
        orDeleted(q);

        return q.field(fieldName).equal(fieldValue).asList();
    }

    public T create(T obj) {
        if (obj == null) {
            return null;
        }
        obj.created = ZonedDateTime.now();
        obj.version = 0L;
        super.save(obj);

        return obj;
    }

    public List<T> create(List<T> objects) {
        if (!CollectionUtils.isEmpty(objects)) {
            ZonedDateTime created = ZonedDateTime.now();
            for (T object : objects) {
                object.created = created;
                object.version = 0L;
            }
            getDatastore().save(objects);
        }
        return objects;
    }

    public T merge(T obj) {
        if (obj == null) {
            return null;
        }
        obj.modified = ZonedDateTime.now();
        getDatastore().merge(obj);
        return obj;
    }

    public T delete(ObjectId id) {
        if (id == null) {
            return null;
        }
        Query<T> q = createQuery().field(MongoCommonModel.FIELDS.id.name()).equal(id);
        UpdateOperations<T> ops = createUpdateOperations().set(MongoCommonModel.FIELDS.deleted.name(), Boolean.TRUE);

        return getDatastore().findAndModify(q, ops);
    }

    public boolean exists(ObjectId id) {
        CountOptions countOptions = new CountOptions();
        countOptions.limit(1);

        return createQuery().field("_id").equal(id).count(countOptions) > 0;
    }

    @Nullable
    public T getSelectedFieldsById(ObjectId id, String[] fieldNames) {
        if (fieldNames == null || fieldNames.length == 0) {
            return null;
        }

        Query<T> query = createQuery();

        for (String fieldName : fieldNames) {
            query.project(fieldName, true);
        }

        query.and(
                query.criteria(MongoCommonModel.FIELDS.id.name()).equal(id),
                orDeleted(query)
        );

        return query.get();
    }

    private CriteriaContainer orDeleted(Query<T> query) {
        return query.or(
                query.criteria(MongoCommonModel.FIELDS.deleted.name()).doesNotExist(),
                query.criteria(MongoCommonModel.FIELDS.deleted.name()).equal(Boolean.FALSE)
        );
    }
}
