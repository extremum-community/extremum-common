package com.extremum.common.models;

import com.extremum.common.descriptor.Descriptor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Transient;
import javax.persistence.Version;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class PersistableCommonModelTest {
    @Test
    void testCopyServiceFieldsTo() {
        Descriptor descriptor = Descriptor.builder()
                .externalId(UUID.randomUUID().toString())
                .build();
        TestPersistableModel from = new TestPersistableModel();
        from.setUuid(descriptor);
        from.setId(UUID.randomUUID());
        from.setCreated(ZonedDateTime.now());
        from.setModified(ZonedDateTime.now());
        from.setVersion(1L);
        from.setDeleted(true);

        TestPersistableModel to = new TestPersistableModel();

        from.copyServiceFieldsTo(to);

        assertThat(to.getUuid(), is(sameInstance(from.getUuid())));
        assertThat(to.getCreated(), is(sameInstance(from.getCreated())));
        assertThat(to.getModified(), is(sameInstance(from.getModified())));
        assertThat(to.getVersion(), is(sameInstance(from.getVersion())));
        assertThat(to.getDeleted(), is(sameInstance(from.getDeleted())));
    }

    @Getter
    @Setter
    private static class TestPersistableModel implements PersistableCommonModel<UUID> {
        private Descriptor uuid;
        private UUID id;

        private ZonedDateTime created;
        private ZonedDateTime modified;
        private Long version;
        private Boolean deleted = false;
    }
}