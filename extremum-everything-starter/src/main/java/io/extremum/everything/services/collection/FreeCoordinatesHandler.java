package io.extremum.everything.services.collection;

import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.services.FreeCollectionFetcher;
import io.extremum.everything.services.FreeCollectionStreamer;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.CollectionCoordinates;
import io.extremum.sharedmodels.descriptor.FreeCoordinates;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@RequiredArgsConstructor
class FreeCoordinatesHandler implements CoordinatesHandler {
    private final List<FreeCollectionFetcher<? extends Model>> freeCollectionFetchers;
    private final List<FreeCollectionStreamer<? extends Model>> freeCollectionStreamers;

    @Override
    public CollectionFragment<Model> fetchCollection(CollectionCoordinates coordinates, Projection projection) {
        FreeCoordinates freeCoordinates = coordinates.getFreeCoordinates();
        FreeCollectionFetcher<? extends Model> fetcher = findFreeFetcher(freeCoordinates);
        return fetcher.fetchCollection(freeCoordinates.getParametersString(), projection)
                .map(Function.identity());
    }

    private FreeCollectionFetcher<? extends Model> findFreeFetcher(FreeCoordinates freeCoordinates) {
        String freeCollectionName = freeCoordinates.getName();
        return freeCollectionFetchers.stream()
                .filter(fetcher -> fetcherSupportsName(fetcher, freeCollectionName))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Did not find a free collection fetcher supporting name '%s'",
                                freeCollectionName)));
    }

    private boolean fetcherSupportsName(FreeCollectionFetcher<?> fetcher, String freeCollectionName) {
        return Objects.equals(fetcher.getCollectionName(), freeCollectionName);
    }

    @Override
    public Flux<Model> streamCollection(CollectionCoordinates coordinates, Projection projection) {
        FreeCoordinates freeCoordinates = coordinates.getFreeCoordinates();
        FreeCollectionStreamer<? extends Model> streamer = findFreeStreamer(freeCoordinates);
        return streamer.streamCollection(freeCoordinates.getParametersString(), projection)
                .map(Function.identity());
    }

    private FreeCollectionStreamer<? extends Model> findFreeStreamer(FreeCoordinates freeCoordinates) {
        String freeCollectionName = freeCoordinates.getName();
        return freeCollectionStreamers.stream()
                .filter(streamer -> streamerSupportsName(streamer, freeCollectionName))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Did not find a free collection streamer supporting name '%s'",
                                freeCollectionName)));
    }

    private boolean streamerSupportsName(FreeCollectionStreamer<?> streamer, String freeCollectionName) {
        return Objects.equals(streamer.getCollectionName(), freeCollectionName);
    }
}
