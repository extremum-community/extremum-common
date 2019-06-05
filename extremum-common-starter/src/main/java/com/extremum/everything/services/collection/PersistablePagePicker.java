package com.extremum.everything.services.collection;

import com.extremum.common.models.BasicModel;
import com.extremum.common.models.Model;
import com.extremum.common.models.PersistableCommonModel;
import com.extremum.common.utils.ModelUtils;
import com.extremum.everything.collection.Projection;
import com.extremum.everything.exceptions.EverythingEverythingException;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rpuch
 */
final class PersistablePagePicker extends AbstractPagePicker<PersistableCommonModel> {
    @Override
    List<PersistableCommonModel> convertToModels(Collection<?> nonEmptyCollection, Model host, String hostAttributeName) {
        return nonEmptyCollection.stream()
                .map(element -> convertElementToPersistableModel(element, host, hostAttributeName))
                .collect(Collectors.toList());
    }

    private PersistableCommonModel convertElementToPersistableModel(Object element, Model host,
            String hostAttributeName) {
        if (!(element instanceof PersistableCommonModel)) {
            String name = ModelUtils.getModelName(host);
            String message = String.format("For entity '%s', field name '%s', collection elements must be String," +
                            " ObjectId, or PersistableCommonModel instances, but encountered '%s'",
                    name, hostAttributeName, element.getClass());
            throw new EverythingEverythingException(message);
        }

        return (PersistableCommonModel) element;
    }

    @Override
    Comparator<PersistableCommonModel> createModelsComparator() {
        Comparator<PersistableCommonModel> compareByCreated = Comparator.comparing(PersistableCommonModel::getCreated,
                Comparator.nullsFirst(Comparator.naturalOrder()));
        return compareByCreated
                .thenComparing(BasicModel::getId, Comparator.nullsFirst(new IdComparator()));
    }

    final List<Model> filter(List<PersistableCommonModel> nonEmptyFullList, Projection projection) {
        return nonEmptyFullList.stream()
                .filter(projection::accepts)
                .filter(model -> model.getDeleted() == null || !model.getDeleted())
                .collect(Collectors.toList());
    }
}
