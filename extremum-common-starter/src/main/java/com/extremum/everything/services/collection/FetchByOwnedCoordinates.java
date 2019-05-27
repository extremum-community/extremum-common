package com.extremum.everything.services.collection;

import com.extremum.common.descriptor.Descriptor;
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

import java.io.Serializable;
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

    public List<Model> fetchCollection(PersistableCommonModel host, String hostFieldName, Projection projection) {
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
            PersistableCommonModel host, Field field) {
        makeSureStorageTypeIsSupported(host, field);
        List<?> ids = convertIdsToDatabaseTypes(collection, host, field);
        return loadModelsByIds(ids, projection, host, field);
    }

    private void makeSureStorageTypeIsSupported(PersistableCommonModel host, Field field) {
        if (host.getUuid() != null && host.getUuid().getStorageType() != Descriptor.StorageType.MONGO) {
            String message = String.format(
                    "Only Mongo models can use IDs to fetch collecitons, but it was '%s' on '%s', field '%s'",
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
            String message = String.format("For host type '%s' field '%s' does not contain @CollectionElementType annotation",
                    name, field.getName());
            throw new EverythingEverythingException(message);
        }

        return elementTypeAnn.value();
    }

    private List<Model> getModelsFromModelsCollection(Collection<?> collection, Projection projection, Model host,
                                                      String hostFieldName) {
        List<PersistableCommonModel> fullList = collection.stream()
                .map(element -> convertElementToPersistableModel(element, host, hostFieldName))
                .collect(Collectors.toList());

        List<PersistableCommonModel> sortedFullList = sortModels(fullList);

        return filterAndProject(sortedFullList, projection);
    }

    private PersistableCommonModel convertElementToPersistableModel(Object element, Model host, String hostFieldName) {
        if (!(element instanceof PersistableCommonModel)) {
            String name = ModelUtils.getModelName(host);
            String message = String.format("For entity '%s', field name '%s', collection elements must be String," +
                            " ObjectId, or Model instances, but encountered '%s'", name, hostFieldName,
                    element.getClass());
            throw new EverythingEverythingException(message);
        }

        return (PersistableCommonModel) element;
    }

    private List<PersistableCommonModel> sortModels(List<PersistableCommonModel> fullList) {
        List<PersistableCommonModel> sortedFullList = new ArrayList<>(fullList);

        Comparator<PersistableCommonModel> compareByCreated = Comparator.comparing(PersistableCommonModel::getCreated,
                Comparator.nullsFirst(Comparator.naturalOrder()));
        Comparator<PersistableCommonModel> comparator = compareByCreated
                .thenComparing(PersistableCommonModel::getId, Comparator.nullsFirst(new IdComparator()));
        sortedFullList.sort(comparator);

        return sortedFullList;
    }

    private List<Model> filterAndProject(List<PersistableCommonModel> fullList, Projection projection) {
        if (fullList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Model> filteredList = filter(fullList, projection);
        return projection.cut(filteredList);
    }

    private List<Model> filter(List<PersistableCommonModel> nonEmptyFullList, Projection projection) {
        return nonEmptyFullList.stream()
                .map(PersistableCommonModel.class::cast)
                .filter(projection::accepts)
                .filter(model -> model.getDeleted() == null || !model.getDeleted())
                .collect(Collectors.toList());
    }

    private static class IdComparator implements Comparator<Serializable> {
        private Class<?> elementClass;

        @Override
        public int compare(Serializable o1, Serializable o2) {
            rememberOrCheckElementClass(o1);
            rememberOrCheckElementClass(o2);

            if (o1 instanceof Comparable) {
                @SuppressWarnings("unchecked") int comparisonResult = ((Comparable) o1).compareTo(o2);
                return comparisonResult;
            }

            return o1.toString().compareTo(o2.toString());
        }

        private void rememberOrCheckElementClass(Object obj) {
            if (obj == null) {
                return;
            }
            Class<?> objClass = obj.getClass();
            if (elementClass == null) {
                elementClass = objClass;
            } else {
                if (objClass != elementClass) {
                    String message = String.format("This comparator only supports comparing elements of the same " +
                                    "type, but it was given '%s' and '%s'", elementClass, objClass);
                    throw new IllegalStateException(message);
                }
            }
        }
    }
}
