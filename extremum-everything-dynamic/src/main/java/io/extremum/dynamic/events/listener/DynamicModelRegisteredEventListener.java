package io.extremum.dynamic.events.listener;

import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.extremum.common.model.VersionedModel;
import io.extremum.dynamic.events.DynamicModelRegisteredEvent;
import io.extremum.sharedmodels.basic.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.reactivestreams.Publisher;
import org.springframework.context.ApplicationListener;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static io.extremum.dynamic.DynamicModelSupports.collectionNameFromModel;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicModelRegisteredEventListener implements ApplicationListener<DynamicModelRegisteredEvent> {
    private final ReactiveMongoOperations operations;

    @Override
    public void onApplicationEvent(DynamicModelRegisteredEvent event) {
        log.info("Creating indexes for model {}", event.getModelName());

        Publisher<String> pIdx1 = operations.getCollection(collectionNameFromModel(event.getModelName()))
                .createIndex(
                        Indexes.compoundIndex(
                                new Document(VersionedModel.FIELDS.lineageId.name(), 1),
                                new Document(Model.FIELDS.version.name(), 1)
                        ),
                        new IndexOptions().unique(true)
                );

        Publisher<String> pIdx2 = operations.getCollection(collectionNameFromModel(event.getModelName()))
                .createIndex(
                        Indexes.compoundIndex(
                                new Document(VersionedModel.FIELDS.lineageId.name(), 1),
                                new Document(VersionedModel.FIELDS.currentSnapshot.name(), 1),
                                new Document(Model.FIELDS.deleted.name(), 1)
                        ),
                        new IndexOptions().unique(true)
                );


        Flux.fromArray(new Publisher[]{pIdx1, pIdx2}).subscribe();
    }
}
