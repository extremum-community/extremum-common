package com.extremum.everything.services.collection;

import com.extremum.common.models.BasicModel;
import com.extremum.common.models.Model;
import com.extremum.everything.collection.CollectionFragment;
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
    public final CollectionFragment<Model> getModelsFromModelsCollection(Collection<?> nonEmptyCollection,
            Projection projection, Model host, String hostAttributeName) {
        List<T> fullList = convertToModels(nonEmptyCollection, host, hostAttributeName);
        List<T> sortedFullList = sortModels(fullList);
        return filterAndProject(sortedFullList, projection);
    }

    abstract List<T> convertToModels(Collection<?> nonEmptyCollection, Model host, String hostAttributeName);

    private List<T> sortModels(List<T> fullList) {
        List<T> sortedFullList = new ArrayList<>(fullList);
        sortedFullList.sort(createModelsComparator());
        return sortedFullList;
    }

    abstract Comparator<T> createModelsComparator();

    private CollectionFragment<Model> filterAndProject(List<T> nonEmptyFullList, Projection projection) {
        List<Model> filteredList = filter(nonEmptyFullList, projection);
        List<Model> fragmentElements = projection.cut(filteredList);
        return CollectionFragment.forFragment(fragmentElements, filteredList.size());
    }

    abstract List<Model> filter(List<T> nonEmptyFullList, Projection projection);
}
