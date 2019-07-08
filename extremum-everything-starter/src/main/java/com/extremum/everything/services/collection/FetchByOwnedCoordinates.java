package com.extremum.everything.services.collection;

import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.common.models.BasicModel;
import com.extremum.common.models.Model;
import com.extremum.common.models.PersistableCommonModel;
import com.extremum.common.utils.InstanceFields;
import com.extremum.common.utils.ModelUtils;
import com.extremum.everything.collection.CollectionFragment;
import com.extremum.everything.collection.Projection;
import com.extremum.everything.dao.UniversalDao;
import com.extremum.everything.exceptions.EverythingEverythingException;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
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
        HostAttribute attribute = getAttribute(host, hostAttributeName);

        Object attributeValue = getGetterValue(host, attribute);
        if (attributeValue == null) {
            return CollectionFragment.emptyWithZeroTotal();
        }

        Collection<?> collection = asCollection(attributeValue, host, attribute);
        if (collection.isEmpty()) {
            return CollectionFragment.emptyWithZeroTotal();
        }

        if (collectionContainsIds(collection)) {
            return loadModelsByIdsCollection(collection, projection, host, attribute);
        }

        return getModelsFromModelsCollection(collection, projection, host, hostAttributeName);
    }

    private HostAttribute getAttribute(BasicModel host, String hostAttributeName) {
        Field field = findField(host, hostAttributeName);
        Method getter = findGetter(host, hostAttributeName);
        return new HostAttribute(hostAttributeName, getter, field);
    }

    @Nullable
    private Field findField(Object object, String fieldName) {
        return new InstanceFields(object.getClass()).stream()
                .filter(field -> Objects.equals(field.getName(), fieldName))
                .findFirst()
                .orElse(null);
    }

    private Method findGetter(Model host, String propertyName) {
        final String getterName = "get" + StringUtils.capitalize(propertyName);

        try {
            return host.getClass().getMethod(getterName);
        } catch (NoSuchMethodException e) {
            throw new EverythingEverythingException(
                    String.format("No method '%s' was found in class '%s'", getterName, host.getClass()));
        }
    }

    private Object getGetterValue(Model host, HostAttribute attribute) {
        return invokeGetter(host, attribute.getter());
    }

    private Object invokeGetter(Model host, Method method) {
        try {
            return method.invoke(host);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Cannot invoke a getter", e);
        }
    }

    private Collection<?> asCollection(Object attributeValue, Model host, HostAttribute attribute) {
        if (!(attributeValue instanceof Collection)) {
            String name = ModelUtils.getModelName(host);
            String message = String.format("'%s' attribute on '%s' contains '%s' and not a Collection",
                    attribute.name(), name, attributeValue.getClass());
            throw new EverythingEverythingException(message);
        }
        return (Collection<?>) attributeValue;
    }

    private boolean collectionContainsIds(Collection<?> collection) {
        Object firstElement = collection.iterator().next();
        return firstElement instanceof ObjectId || firstElement instanceof String;
    }

    private CollectionFragment<Model> loadModelsByIdsCollection(Collection<?> collection, Projection projection,
            BasicModel host, HostAttribute attribute) {
        makeSureStorageTypeIsSupported(host, attribute);
        List<?> ids = convertIdsToDatabaseTypes(collection, host, attribute);
        return loadModelsByIds(ids, projection, host, attribute);
    }

    private void makeSureStorageTypeIsSupported(BasicModel host, HostAttribute attribute) {
        if (host.getUuid() != null && host.getUuid().getStorageType() != Descriptor.StorageType.MONGO) {
            String message = String.format(
                    "Only Mongo models can use IDs to fetch collections, but it was '%s' on '%s', attribute '%s'",
                    host.getUuid().getStorageType(), ModelUtils.getModelName(host), attribute.name());
            throw new IllegalStateException(message);
        }
    }

    private List<?> convertIdsToDatabaseTypes(Collection<?> collection, Model host, HostAttribute attribute) {
        Class<? extends Model> elementClass = attribute.detectElementClass(host);
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

    private CollectionFragment<Model> loadModelsByIds(List<?> ids, Projection projection, Model host,
            HostAttribute attribute) {
        Class<? extends Model> classOfElement = attribute.detectElementClass(host);
        return universalDao.retrieveByIds(ids, classOfElement, projection)
                .map(Function.identity());
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

        return new PlainPagePicker();
    }

}
