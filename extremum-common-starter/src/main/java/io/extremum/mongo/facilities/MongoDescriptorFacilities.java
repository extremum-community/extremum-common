package io.extremum.mongo.facilities;

import io.extremum.sharedmodels.descriptor.Descriptor;
import org.bson.types.ObjectId;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author rpuch
 */
public interface MongoDescriptorFacilities {
    Descriptor create(ObjectId id, String modelType);

    Descriptor fromInternalId(ObjectId internalId);

    Descriptor fromInternalId(String internalId);

    List<String> getInternalIdList(List<Descriptor> descriptors);

    List<Descriptor> fromInternalIdListOrNull(List<String> internalIdList);

    ObjectId resolve(Descriptor descriptor);

    Descriptor makeDescriptorReady(String descriptorExternalId, String modelType);
}
