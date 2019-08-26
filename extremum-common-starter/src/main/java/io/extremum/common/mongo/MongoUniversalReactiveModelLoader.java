package io.extremum.common.mongo;

import io.extremum.common.models.Model;
import io.extremum.common.models.PersistableCommonModel;
import io.extremum.common.support.UniversalReactiveModelLoader;
import io.extremum.common.utils.ModelUtils;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@RequiredArgsConstructor
public class MongoUniversalReactiveModelLoader implements UniversalReactiveModelLoader {
    private final ReactiveMongoOperations reactiveMongoOperations;
    private final SoftDeletion softDeletion = new SoftDeletion();

    @Override
    public Mono<Model> loadByInternalId(String internalId, Class<? extends Model> modelClass) {
        Query query = criteriaToSearchById(internalId, modelClass);
        return reactiveMongoOperations.findOne(query, modelClass)
                .map(Function.identity());
    }

    private Query criteriaToSearchById(String id, Class<? extends Model> modelClass) {
        List<Criteria> criteria = new ArrayList<>();

        criteria.add(where(PersistableCommonModel.FIELDS.id.name()).is(new ObjectId(id)));
        if (ModelUtils.isSoftDeletable(modelClass)) {
            criteria.add(softDeletion.notDeleted());
        }

        return new Query(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
    }

    @Override
    public Descriptor.StorageType type() {
        return Descriptor.StorageType.MONGO;
    }
}
