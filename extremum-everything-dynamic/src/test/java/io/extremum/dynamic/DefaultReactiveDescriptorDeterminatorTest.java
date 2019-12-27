package io.extremum.dynamic;

import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DefaultReactiveDescriptorDeterminatorTest {
    private static String modelNameOfADynamicModel = "DynamicModel";

    static DefaultReactiveDescriptorDeterminator determinator = new DefaultReactiveDescriptorDeterminator();

    @BeforeAll
    static void beforeAll() {
        determinator.registerDynamicModel(modelNameOfADynamicModel);
    }

    @Test
    void determineAsADynamicModelDescriptor_if_DescriptorIsForADynamicModel() {
        Descriptor descriptorForADynamicModel = Descriptor.builder()
                .internalId("i-id")
                .externalId("e-id")
                .modelType(modelNameOfADynamicModel)
                .build();

        boolean result = determinator.isDescriptorForDynamicModel(descriptorForADynamicModel).block();

        Assertions.assertTrue(result);
    }

    @Test
    void determineAsNotADynamicModelDescriptor_if_DescriptorIsNotForADynamicModel() {
        Descriptor descriptorForANonDynamicModel = Descriptor.builder()
                .internalId("i-id")
                .externalId("e-id")
                .modelType("NotDynamicModel")
                .build();

        boolean result = determinator.isDescriptorForDynamicModel(descriptorForANonDynamicModel).block();

        Assertions.assertFalse(result);
    }
}