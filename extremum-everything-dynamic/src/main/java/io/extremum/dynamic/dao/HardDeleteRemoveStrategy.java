package io.extremum.dynamic.dao;

import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Mono;

import static com.mongodb.client.model.Filters.eq;

@RequiredArgsConstructor
public class HardDeleteRemoveStrategy implements DynamicModelRemoveStrategy {
    private final ReactiveMongoOperations mongoOperations;

    @Override
    public Mono<Void> remove(Descriptor id, String collectionName) {
        return id.getInternalIdReactively()
                .map(ObjectId::new)
                .flatMap(oId -> doRemove(oId, collectionName));
    }

    private Mono<Void> doRemove(ObjectId oId, String collectionName) {
        return Mono.from(mongoOperations.getCollection(collectionName)
                .deleteOne(eq("_id", oId)))
                .then();
    }

}
