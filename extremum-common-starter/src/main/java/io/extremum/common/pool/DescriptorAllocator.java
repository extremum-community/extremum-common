package io.extremum.common.pool;

import io.extremum.common.descriptor.factory.BlankDescriptorSaver;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class DescriptorAllocator implements Allocator<Descriptor> {
    private final BlankDescriptorSaver blankDescriptorSaver;
    private final Descriptor.StorageType storageType;
    private final InternalIdGenerator internalIdGenerator;

    @Override
    public List<Descriptor> allocate(int quantityToAllocate) {
        List<String> internalIds = IntStream.range(0, quantityToAllocate)
                .mapToObj(i -> internalIdGenerator.generateRandomInternalId())
                .collect(Collectors.toList());
        return blankDescriptorSaver.createAndSaveBatchOfBlankDescriptors(internalIds, storageType);
    }
}
