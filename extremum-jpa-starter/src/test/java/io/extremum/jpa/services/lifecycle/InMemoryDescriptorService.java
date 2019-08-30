package io.extremum.jpa.services.lifecycle;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.utils.ReflectionUtils;
import io.extremum.common.uuid.StandardUUIDGenerator;
import io.extremum.common.uuid.UUIDGenerator;
import io.extremum.sharedmodels.descriptor.Descriptor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * @author rpuch
 */
public class InMemoryDescriptorService implements DescriptorService {
    private final UUIDGenerator uuidGenerator = new StandardUUIDGenerator();

    @Override
    public String createExternalId() {
        return uuidGenerator.generateUUID();
    }

    @Override
    public Descriptor store(Descriptor descriptor) {
        String externalId = ReflectionUtils.getFieldValue(descriptor, "externalId");
        if (externalId == null) {
            externalId = createExternalId();
            ReflectionUtils.setFieldValue(descriptor, "externalId", externalId);
        }

        return descriptor;
    }

    @Override
    public Optional<Descriptor> loadByExternalId(String externalId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Descriptor> loadByInternalId(String internalId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> loadMapByExternalIds(Collection<String> externalIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> loadMapByInternalIds(Collection<String> internalIds) {
        throw new UnsupportedOperationException();
    }
}
