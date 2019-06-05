package com.extremum.everything.services.collection;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.models.BasicModel;
import com.extremum.common.models.Model;
import com.extremum.common.models.PersistableCommonModel;
import com.extremum.common.utils.EntityUtils;
import com.extremum.common.utils.InstanceFields;
import com.extremum.common.utils.ModelUtils;
import com.extremum.common.utils.ReflectionUtils;
import com.extremum.everything.collection.CollectionElementType;
import com.extremum.everything.collection.CollectionFragment;
import com.extremum.everything.collection.Projection;
import com.extremum.everything.dao.UniversalDao;
import com.extremum.everything.exceptions.EverythingEverythingException;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
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

    public CollectionFragment<Model> fetchCollection(BasicModel host, String hostAttributeName, Projection projection) {
        Field field = findField(host, hostAttributeName);

        Object fieldValue = getPropertyValue(host, field);
        if (fieldValue == null) {
            return CollectionFragment.emptyWithZeroTotal();
        }

        Collection<?> collection = asCollection(fieldValue, host, hostAttributeName);
        if (collection.isEmpty()) {
            return CollectionFragment.emptyWithZeroTotal();
        }

        if (collectionContainsIds(collection)) {
            return loadModelsByIdsCollection(collection, projection, host, field);
        }

        return getModelsFromModelsCollection(collection, projection, host, hostAttributeName);
    }

    private Field findField(Object object, String fieldName) {
        return new InstanceFields(object.getClass()).stream()
                .filter(field -> Objects.equals(field.getName(), fieldName))
                .findFirst()
                .orElseThrow(() -> new EverythingEverythingException(
                        String.format("No field '%s' was found in class '%s'", fieldName, object.getClass()))
                );
    }

    private Object getPropertyValue(Model host, Field field) {
        if (EntityUtils.isProxyClass(host.getClass())) {
            return getGetterValue(host, field.getName());
        }

        return getFieldValue(host, field);
    }

    private Object getGetterValue(Model host, String propertyName) {
        Method method = findGetter(host, propertyName);
        return invokeGetter(host, method);
    }

    private Method findGetter(Model host, String propertyName) {
        final String getterName = "get" + StringUtils.capitalize(propertyName);

        try {
            return host.getClass().getMethod(getterName);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Cannot find a getter", e);
        }
    }

    private Object invokeGetter(Model host, Method method) {
        try {
            return method.invoke(host);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Cannot invoke a getter", e);
        }
    }

    private Object getFieldValue(Model host, Field field) {
        return ReflectionUtils.getFieldValue(field, host);
    }

    private Collection<?> asCollection(Object fieldValue, Model host, String hostAttributeName) {
        if (!(fieldValue instanceof Collection)) {
            String name = ModelUtils.getModelName(host);
            String message = String.format("'%s' field on '%s' contains '%s' and not a Collection", hostAttributeName,
                    name, fieldValue.getClass());
            throw new EverythingEverythingException(message);
        }
        return (Collection<?>) fieldValue;
    }

    private boolean collectionContainsIds(Collection<?> collection) {
        Object firstElement = collection.iterator().next();
        return firstElement instanceof ObjectId || firstElement instanceof String;
    }

    private CollectionFragment<Model> loadModelsByIdsCollection(Collection<?> collection, Projection projection,
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

    private CollectionFragment<Model> loadModelsByIds(List<?> ids, Projection projection, Model host, Field field) {
        Class<? extends Model> classOfElement = detectElementClass(host, field);
        return universalDao.retrieveByIds(ids, classOfElement, projection)
                .map(Function.identity());
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

    private CollectionFragment<Model> getModelsFromModelsCollection(Collection<?> nonEmptyCollection,
            Projection projection, Model host, String hostAttributeName) {
        PagePicker pagePicker = detectPagePicker(nonEmptyCollection);
        return pagePicker.getModelsFromModelsCollection(nonEmptyCollection, projection, host, hostAttributeName);
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
