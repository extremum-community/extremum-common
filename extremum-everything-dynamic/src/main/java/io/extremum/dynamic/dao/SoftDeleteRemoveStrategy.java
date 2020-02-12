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

@RequiredArgsConstructor
public class SoftDeleteRemoveStrategy implements DynamicModelRemoveStrategy {
    private final ReactiveMongoOperations mongoOperations;

    @Override
    public Mono<Void> remove(Descriptor id, String collectionName) {
        Publisher<UpdateResult> result = mongoOperations.getCollection(collectionName)
                .updateOne(
                        and(
                                eq("_id", new ObjectId(id.getInternalId())),
                                or(
                                        eq(deleted.name(), false),
                                        exists(deleted.name(), false)
                                )
                        ),
                        set(deleted.name(), true)
                );

        return Mono.from(result).then();
    }
}
