package com.extremum.everything.services.collection;

import com.extremum.common.models.Model;
import com.extremum.common.utils.ModelUtils;
import com.extremum.everything.collection.Projection;
import com.extremum.everything.exceptions.EverythingEverythingException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
final class PlainPagePicker extends AbstractPagePicker<Model> {
    @Override
    List<Model> convertToModels(Collection<?> nonEmptyCollection, Model host, String hostAttributeName) {
        return nonEmptyCollection.stream()
                .map(element -> convertElementToModel(element, host, hostAttributeName))
                .collect(Collectors.toList());
    }

    @Override
    List<Model> sortModelsIfPossible(List<Model> fullList) {
        return fullList;
    }

    private Model convertElementToModel(Object element, Model host, String hostAttributeName) {
        if (!(element instanceof Model)) {
            String name = ModelUtils.getModelName(host);
            String message = String.format("For entity '%s', field name '%s', collection elements must be String," +
                            " ObjectId, or Model instances, but encountered '%s'", name, hostAttributeName,
                    element.getClass());
            throw new EverythingEverythingException(message);
        }

        return (Model) element;
    }

    final List<Model> filterIsPossible(List<Model> nonEmptyFullList, Projection projection) {
        if (projection.definesFilteringOnCreationDate()) {
            throw new EverythingEverythingException(
                    "Cannot filter plain Model instances on creation date; please do not specify since/until");
        }

        return new ArrayList<>(nonEmptyFullList);
    }
}
