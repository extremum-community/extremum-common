package io.extremum.everything.services.collection;

import io.extremum.common.models.Model;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.dao.UniversalDao;

import java.util.List;
import java.util.function.Function;

/**
 * @author rpuch
 */
public final class FetchByOwnedCoordinates extends ByOwnedCoordinates<CollectionFragment<Model>> {
    private final UniversalDao universalDao;

    public FetchByOwnedCoordinates(UniversalDao universalDao) {
        this.universalDao = universalDao;
    }

    @Override
    CollectionFragment<Model> emptyResult() {
        return CollectionFragment.emptyWithZeroTotal();
    }

    @Override
    CollectionFragment<Model> retrieveModelsByIds(List<?> ids, Class<? extends Model> classOfElement,
                                                  Projection projection) {
        return universalDao.retrieveByIds(ids, classOfElement, projection)
                .map(Function.identity());
    }

    @Override
    CollectionFragment<Model> collectionFragmentToResult(CollectionFragment<Model> fragment) {
        return fragment;
    }

}
