package com.extremum.everything.services.collection;

import com.extremum.common.models.BasicModel;
import com.extremum.common.models.Model;
import com.extremum.everything.collection.Projection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author rpuch
 */
abstract class AbstractPagePicker<T extends BasicModel> implements PagePicker {
    @Override
    public final List<Model> getModelsFromModelsCollection(Collection<?> nonEmptyCollection,
            Projection projection, Model host, String hostFieldName) {
        List<T> fullList = convertToModels(nonEmptyCollection, host, hostFieldName);
        List<T> sortedFullList = sortModels(fullList);
        return filterAndProject(sortedFullList, projection);
    }

    abstract List<T> convertToModels(Collection<?> nonEmptyCollection, Model host, String hostFieldName);

    private List<T> sortModels(List<T> fullList) {
        List<T> sortedFullList = new ArrayList<>(fullList);
        sortedFullList.sort(createModelsComparator());
        return sortedFullList;
    }

    abstract Comparator<T> createModelsComparator();

    private List<Model> filterAndProject(List<T> nonEmptyFullList, Projection projection) {
        List<Model> filteredList = filter(nonEmptyFullList, projection);
        return projection.cut(filteredList);
    }

    abstract List<Model> filter(List<T> nonEmptyFullList, Projection projection);
}
