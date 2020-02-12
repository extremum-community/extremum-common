package io.extremum.dynamic.dao;

import com.mongodb.client.result.DeleteResult;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Mono;

import static com.mongodb.client.model.Filters.eq;

@RequiredArgsConstructor
public class HardDeleteRemoveStrategy implements DynamicModelRemoveStrategy {
    private final ReactiveMongoOperations mongoOperations;

    @Override
    public Mono<Void> remove(Descriptor id, String collectionName) {
        Publisher<DeleteResult> result = mongoOperations.getCollection(collectionName)
                .deleteOne(eq("_id", new ObjectId(id.getInternalId())));

        return Mono.from(result).then();
    }
}
