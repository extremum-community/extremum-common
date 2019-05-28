package com.extremum.everything.services.collection;

import com.extremum.common.models.BasicModel;
import com.extremum.common.models.Model;
import com.extremum.common.utils.ModelUtils;
import com.extremum.everything.collection.Projection;
import com.extremum.everything.exceptions.EverythingEverythingException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
final class BasicPagePicker extends AbstractPagePicker<BasicModel> {
    @Override
    List<BasicModel> convertToModels(Collection<?> nonEmptyCollection, Model host, String hostFieldName) {
        return nonEmptyCollection.stream()
                .map(element -> convertElementToBasicModel(element, host, hostFieldName))
                .collect(Collectors.toList());
    }

    private BasicModel convertElementToBasicModel(Object element, Model host, String hostFieldName) {
        if (!(element instanceof BasicModel)) {
            String name = ModelUtils.getModelName(host);
            String message = String.format("For entity '%s', field name '%s', collection elements must be String," +
                            " ObjectId, or BasicModel instances, but encountered '%s'", name, hostFieldName,
                    element.getClass());
            throw new EverythingEverythingException(message);
        }

        return (BasicModel) element;
    }

    @Override
    Comparator<BasicModel> createModelsComparator() {
        return Comparator.comparing(BasicModel::getId, Comparator.nullsFirst(new IdComparator()));
    }

    final List<Model> filter(List<BasicModel> nonEmptyFullList, Projection projection) {
        if (projection.definesFilteringOnCreationDate()) {
            throw new EverythingEverythingException(
                    "Cannot filter BasicModel instances on creation date; please do not specify since/until");
        }

        return new ArrayList<>(nonEmptyFullList);
    }
}
