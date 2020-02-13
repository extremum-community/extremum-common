package io.extremum.dynamic.dao;

import com.mongodb.client.result.UpdateResult;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Mono;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;
import static io.extremum.sharedmodels.basic.Model.FIELDS.deleted;
import static reactor.core.publisher.Mono.from;

@RequiredArgsConstructor
public class SoftDeleteRemoveStrategy implements DynamicModelRemoveStrategy {
    private final ReactiveMongoOperations mongoOperations;

    @Override
    public Mono<Void> remove(Descriptor id, String collectionName) {
        return id.getInternalIdReactively()
                .map(ObjectId::new)
                .flatMap(oId -> from(doRemove(oId, collectionName)))
                .then();
    }

    private Publisher<UpdateResult> doRemove(ObjectId oId, String collectionName) {
        return mongoOperations.getCollection(collectionName)
                .updateOne(
                        and(
                                eq("_id", oId),
                                or(
                                        eq(deleted.name(), false),
                                        exists(deleted.name(), false)
                                )
                        ),
                        set(deleted.name(), true)
                );
    }
}
