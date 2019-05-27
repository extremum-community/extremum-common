package com.extremum.everything.services.collection;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.models.BasicModel;
import com.extremum.common.models.Model;
import com.extremum.common.models.PersistableCommonModel;
import com.extremum.common.utils.InstanceFields;
import com.extremum.common.utils.ModelUtils;
import com.extremum.everything.collection.CollectionElementType;
import com.extremum.everything.collection.Projection;
import com.extremum.everything.dao.UniversalDao;
import com.extremum.everything.exceptions.EverythingEverythingException;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
public class FetchByOwnedCoordinates {
    private final UniversalDao universalDao;
    private final IdConversion idConversion = new IdConversion();

    public FetchByOwnedCoordinates(UniversalDao universalDao) {
        this.universalDao = universalDao;
    }

    public List<Model> fetchCollection(BasicModel host, String hostFieldName, Projection projection) {
        Field field = findField(host, hostFieldName);

        Object fieldValue = getFieldValue(host, field);
        if (fieldValue == null) {
            return Collections.emptyList();
        }

        Collection<?> collection = asCollection(fieldValue, host, hostFieldName);
        if (collection.isEmpty()) {
            return Collections.emptyList();
        }

        if (collectionContainsIds(collection)) {
            return loadModelsByIdsCollection(collection, projection, host, field);
        }

        return getModelsFromModelsCollection(collection, projection, host, hostFieldName);
    }

    private Field findField(Object object, String fieldName) {
        return new InstanceFields(object.getClass()).stream()
                .filter(field -> Objects.equals(field.getName(), fieldName))
                .findFirst()
                .orElseThrow(() -> new EverythingEverythingException(
                        String.format("No field '%s' was found in class '%s'", fieldName, object.getClass()))
                );
    }

    private Object getFieldValue(Model host, Field field) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }

        try {
            return field.get(host);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot get field value", e);
        }
    }

    private Collection<?> asCollection(Object fieldValue, Model host, String hostFieldName) {
        if (!(fieldValue instanceof Collection)) {
            String name = ModelUtils.getModelName(host);
            String message = String.format("'%s' field on '%s' contains '%s' and not a Collection", hostFieldName,
                    name, fieldValue.getClass());
            throw new EverythingEverythingException(message);
        }
        return (Collection<?>) fieldValue;
    }

    private boolean collectionContainsIds(Collection<?> collection) {
        Object firstElement = collection.iterator().next();
        return firstElement instanceof ObjectId || firstElement instanceof String;
    }

    private List<Model> loadModelsByIdsCollection(Collection<?> collection, Projection projection,
            BasicModel host, Field field) {
        makeSureStorageTypeIsSupported(host, field);
        List<?> ids = convertIdsToDatabaseTypes(collection, host, field);
        return loadModelsByIds(ids, projection, host, field);
    }

    private void makeSureStorageTypeIsSupported(BasicModel host, Field field) {
        if (host.getUuid() != null && host.getUuid().getStorageType() != Descriptor.StorageType.MONGO) {
            String message = String.format(
                    "Only Mongo models can use IDs to fetch collections, but it was '%s' on '%s', field '%s'",
                    host.getUuid().getStorageType(), ModelUtils.getModelName(host), field.getName());
            throw new IllegalStateException(message);
        }
    }

    private List<?> convertIdsToDatabaseTypes(Collection<?> collection, Model host, Field field) {
        Class<? extends Model> elementClass = detectElementClass(host, field);
        Class<?> elementIdClass = detectIdClass(elementClass);

        return collection.stream()
                .map(id -> idConversion.convert(id, elementIdClass))
                .collect(Collectors.toList());
    }

    private Class<?> detectIdClass(Class<? extends Model> elementClass) {
        List<Field> idFields = new InstanceFields(elementClass).stream()
                .filter(field -> field.isAnnotationPresent(Id.class))
                .collect(Collectors.toList());
        if (idFields.size() != 1) {
            throw new EverythingEverythingException(
                    String.format("'%s' defines %d @Id fields, must be exactly 1", elementClass, idFields.size()));
        }

        Field idField = idFields.get(0);
        return idField.getType();
    }

    private List<Model> loadModelsByIds(List<?> ids, Projection projection, Model host, Field field) {
        Class<? extends Model> classOfElement = detectElementClass(host, field);
        return new ArrayList<>(universalDao.retrieveByIds(ids, classOfElement, projection));
    }

    private Class<? extends Model> detectElementClass(Model host, Field field) {
        CollectionElementType elementTypeAnn = field.getAnnotation(CollectionElementType.class);
        if (elementTypeAnn == null) {
            String name = ModelUtils.getModelName(host);
            String message = String.format(
                    "For host type '%s' field '%s' does not contain @CollectionElementType annotation",
                    name, field.getName());
            throw new EverythingEverythingException(message);
        }

        return elementTypeAnn.value();
    }

    private List<Model> getModelsFromModelsCollection(Collection<?> nonEmptyCollection, Projection projection,
            Model host, String hostFieldName) {
        PagePicker pagePicker = detectPagePicker(nonEmptyCollection);
        return pagePicker.getModelsFromModelsCollection(nonEmptyCollection, projection, host, hostFieldName);
    }

    private PagePicker detectPagePicker(Collection<?> nonEmptyCollection) {
        Object firstElement = nonEmptyCollection.iterator().next();
        if (firstElement instanceof PersistableCommonModel) {
            return new PersistablePagePicker();
        }
        if (firstElement instanceof BasicModel) {
            return new BasicPagePicker();
        }

        throw new EverythingEverythingException(
                "Only instances of BasicModel are supported as elements of a collection");
    }
}
