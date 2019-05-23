package com.extremum.common.models;

import com.extremum.common.descriptor.Descriptor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class BasicModelTest {
    @Test
    void testCopyServiceFieldsTo() {
        Descriptor descriptor = Descriptor.builder()
                .externalId(UUID.randomUUID().toString())
                .build();
        TestBasicModel from = new TestBasicModel();
        from.setUuid(descriptor);
        from.setId(UUID.randomUUID());

        TestBasicModel to = new TestBasicModel();

        from.copyServiceFieldsTo(to);

        assertThat(to.getUuid(), is(sameInstance(from.getUuid())));
    }

    @Getter @Setter
    private static class TestBasicModel implements BasicModel<UUID> {
        private Descriptor uuid;
        private UUID id;
    }
}