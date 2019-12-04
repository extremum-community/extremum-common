package io.extremum.common.descriptor.factory.impl;

import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.common.utils.ReflectionUtils;
import io.extremum.common.uuid.StandardUUIDGenerator;
import io.extremum.common.uuid.UUIDGenerator;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/**
 * @author rpuch
 */
public class InMemoryReactiveDescriptorService implements ReactiveDescriptorService {
    private final UUIDGenerator uuidGenerator = new StandardUUIDGenerator();
    private final ConcurrentMap<String, Descriptor> externalIdToDescriptorMap = new ConcurrentHashMap<>();

    @Override
    public Mono<Descriptor> store(Descriptor descriptor) {
        String externalId = ReflectionUtils.getFieldValue(descriptor, "externalId");
        if (externalId == null) {
            externalId = uuidGenerator.generateUUID();
            ReflectionUtils.setFieldValue(descriptor, "externalId", externalId);
        }

        externalIdToDescriptorMap.put(externalId, descriptor);

        return Mono.just(descriptor);
    }

    @Override
    public Mono<Descriptor> loadByExternalId(String externalId) {
        return Mono.justOrEmpty(externalIdToDescriptorMap.get(externalId));
    }

    @Override
    public Mono<Descriptor> loadByInternalId(String internalId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Map<String, String>> loadMapByInternalIds(Collection<String> internalIds) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Stream<Descriptor> descriptors() {
        return externalIdToDescriptorMap.values().stream();
    }
}
