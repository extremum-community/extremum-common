package io.extremum.mongo.facilities;

import io.extremum.common.descriptor.factory.DescriptorFactory;
import io.extremum.common.descriptor.factory.DescriptorResolver;
import io.extremum.common.descriptor.factory.DescriptorSaver;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.stream.Collectors;

import static io.extremum.sharedmodels.descriptor.StandardStorageType.MONGO;

@RequiredArgsConstructor
public final class MongoDescriptorFacilitiesImpl implements MongoDescriptorFacilities {

    private final DescriptorFactory descriptorFactory;
    private final DescriptorSaver descriptorSaver;

    @Override
    public Descriptor create(ObjectId id, String modelType) {
        return descriptorSaver.createAndSave(id.toString(), modelType, MONGO);
    }

    @Override
    public Descriptor fromInternalId(ObjectId internalId) {
        return fromInternalId(internalId.toString());
    }

    @Override
    public Descriptor fromInternalId(String internalId) {
        return descriptorFactory.fromInternalId(internalId, MONGO);
    }

    @Override
    public List<String> getInternalIdList(List<Descriptor> descriptors) {
        return descriptors.stream()
                .map(Descriptor::getInternalId)
                .collect(Collectors.toList());
    }

    private Descriptor fromInternalIdOrNull(String internalId) {
        return descriptorFactory.fromInternalIdOrNull(internalId, MONGO);
    }

    @Override
    public List<Descriptor> fromInternalIdListOrNull(List<String> internalIdList) {
        if (internalIdList == null) {
            return null;
        }
        return internalIdList.stream()
                .map(this::fromInternalIdOrNull)
                .collect(Collectors.toList());
    }

    @Override
    public ObjectId resolve(Descriptor descriptor) {
        String internalId = DescriptorResolver.resolve(descriptor, MONGO);
        return new ObjectId(internalId);
    }
}
