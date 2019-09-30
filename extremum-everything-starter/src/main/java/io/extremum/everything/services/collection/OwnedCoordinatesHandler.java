package io.extremum.everything.services.collection;

import io.extremum.common.model.BasicModel;
import io.extremum.common.reactive.Reactifier;
import io.extremum.common.tx.CollectionTransactivity;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.dao.UniversalDao;
import io.extremum.everything.exceptions.EverythingEverythingException;
import io.extremum.everything.services.OwnedCollectionFetcher;
import io.extremum.everything.services.OwnedCollectionStreamer;
import io.extremum.everything.services.management.ModelRetriever;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.CollectionCoordinates;
import io.extremum.sharedmodels.descriptor.OwnedCoordinates;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@RequiredArgsConstructor
class OwnedCoordinatesHandler implements CoordinatesHandler {
    private final ModelRetriever modelRetriever;
    private final UniversalDao universalDao;
    private final List<OwnedCollectionFetcher> ownedCollectionFetchers;
    private final List<OwnedCollectionStreamer> ownedCollectionStreamers;
    private final Reactifier reactifier;
    private final CollectionTransactivity transactivity;

    @Override
    public CollectionFragment<Model> fetchCollection(CollectionCoordinates coordinates, Projection projection) {
        OwnedCoordinates owned = coordinates.getOwnedCoordinates();
        BasicModel host = retrieveHost(owned);

        Optional<OwnedCollectionFetcher> optFetcher = findFetcher(owned);

        @SuppressWarnings("unchecked")
        CollectionFragment<Model> castResult = optFetcher
                .map(fetcher -> fetcher.fetchCollection(host, projection))
                .orElseGet(() -> fetchUsingDefaultConvention(owned, host, projection));
        return castResult;
    }

    private BasicModel retrieveHost(OwnedCoordinates owned) {
        Model host = modelRetriever.retrieveModel(owned.getHostId());
        if (host == null) {
            throw createHostNotFoundException(owned);
        }

        return castToBasicModel(owned, host);
    }

    private EverythingEverythingException createHostNotFoundException(OwnedCoordinates owned) {
        String message = format("No host entity was found by external ID '%s'",
                owned.getHostId().getExternalId());
        return new EverythingEverythingException(message);
    }

    private BasicModel castToBasicModel(OwnedCoordinates owned, Model host) {
        if (!(host instanceof BasicModel)) {
            throw new EverythingEverythingException(String.format("Host '%s' is not a BasicModel",
                    owned.getHostId().getModelType()));
        }

        return (BasicModel) host;
    }

    private Optional<OwnedCollectionFetcher> findFetcher(OwnedCoordinates owned) {
        return ownedCollectionFetchers.stream()
                        .filter(fetcher -> fetcher.getSupportedModel().equals(owned.getHostId().getModelType()))
                        .filter(fetcher -> fetcher.getHostAttributeName().equals(owned.getHostAttributeName()))
                        .findFirst();
    }

    private CollectionFragment<Model> fetchUsingDefaultConvention(OwnedCoordinates owned,
            BasicModel host, Projection projection) {
        FetchByOwnedCoordinates fetcher = new FetchByOwnedCoordinates(universalDao);
        return fetcher.fetch(host, owned.getHostAttributeName(), projection);
    }

    @Override
    public Flux<Model> streamCollection(CollectionCoordinates coordinates, Projection projection) {
        OwnedCoordinates owned = coordinates.getOwnedCoordinates();
        if (dbAccessShouldBeMadeInATransaction(owned)) {
            return streamBlockingFetchResult(coordinates, projection, owned);
        }

        return streamReactively(projection, owned);
    }

    private boolean dbAccessShouldBeMadeInATransaction(OwnedCoordinates owned) {
        return transactivity.isCollectionTransactional(owned.getHostId());
    }

    private Flux<Model> streamBlockingFetchResult(CollectionCoordinates coordinates, Projection projection,
                                                  OwnedCoordinates owned) {
        return reactifier.flux(() -> {
            return transactivity.doInTransaction(owned.getHostId(), () -> {
                return fetchCollection(coordinates, projection).elements();
            });
        });
    }

    private Flux<Model> streamReactively(Projection projection, OwnedCoordinates owned) {
        Mono<BasicModel> hostMono = retrieveHostReactively(owned);

        Optional<OwnedCollectionStreamer> optStreamer = findStreamer(owned);

        if (optStreamer.isPresent()) {
            OwnedCollectionStreamer streamer = optStreamer.get();
            @SuppressWarnings("unchecked")
            Flux<Model> castModels = hostMono.flatMapMany(host -> streamer.streamCollection(host, projection));
            return castModels;
        } else {
            return fetchUsingDefaultConventionReactively(owned, hostMono, projection);
        }
    }

    private Mono<BasicModel> retrieveHostReactively(OwnedCoordinates coordinates) {
        return modelRetriever.retrieveModelReactively(coordinates.getHostId())
                .switchIfEmpty(Mono.error(() -> createHostNotFoundException(coordinates)))
                .map(host -> castToBasicModel(coordinates, host));
    }

    private Optional<OwnedCollectionStreamer> findStreamer(OwnedCoordinates owned) {
        return ownedCollectionStreamers.stream()
                .filter(streamer -> streamer.getSupportedModel().equals(owned.getHostId().getModelType()))
                .filter(streamer -> streamer.getHostAttributeName().equals(owned.getHostAttributeName()))
                .findFirst();
    }

    private Flux<Model> fetchUsingDefaultConventionReactively(OwnedCoordinates owned,
                                                              Mono<BasicModel> hostMono,
                                                              Projection projection) {
        StreamByOwnedCoordinates streamer = new StreamByOwnedCoordinates(universalDao);
        return hostMono.flatMapMany(host ->
                streamer.stream(host, owned.getHostAttributeName(), projection));
    }
}
