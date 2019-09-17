package io.extremum.elasticsearch.facilities;

import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.common.utils.ReflectionUtils;
import io.extremum.common.uuid.StandardUUIDGenerator;
import io.extremum.common.uuid.UUIDGenerator;
import io.extremum.sharedmodels.descriptor.Descriptor;
import reactor.core.publisher.Mono;

/**
 * @author rpuch
 */
public class InMemoryReactiveDescriptorService implements ReactiveDescriptorService {
    private final UUIDGenerator uuidGenerator = new StandardUUIDGenerator();

    @Override
    public Mono<Descriptor> store(Descriptor descriptor) {
        String externalId = ReflectionUtils.getFieldValue(descriptor, "externalId");
        if (externalId == null) {
            externalId = uuidGenerator.generateUUID();
            ReflectionUtils.setFieldValue(descriptor, "externalId", externalId);
        }

        return Mono.just(descriptor);
    }

    @Override
    public Mono<Descriptor> loadByExternalId(String externalId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Descriptor> loadByInternalId(String internalId) {
        throw new UnsupportedOperationException();
    }
}
