package com.extremum.jpa.services.lifecycle;

import com.extremum.common.descriptor.factory.DescriptorFactory;
import com.extremum.common.descriptor.factory.DescriptorSaver;
import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.jpa.facilities.PostgresqlDescriptorFacilities;
import com.extremum.jpa.facilities.PostgresqlDescriptorFacilitiesImpl;
import com.extremum.jpa.facilities.StaticPostgresqlDescriptorFacilitiesAccessor;
import com.extremum.jpa.models.TestJpaModel;
import com.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class JpaCommonModelLifecycleListenerTest {
    private final JpaCommonModelLifecycleListener listener = new JpaCommonModelLifecycleListener();

    @Spy
    private DescriptorService descriptorService = new InMemoryDescriptorService();

    private final UUID internalId = UUID.randomUUID();
    private final Descriptor descriptor = Descriptor.builder()
            .externalId("external-id")
            .internalId(internalId.toString())
            .modelType("Test")
            .storageType(Descriptor.StorageType.POSTGRES)
            .build();

    @BeforeEach
    void initFacilities() {
        PostgresqlDescriptorFacilities facilities = new PostgresqlDescriptorFacilitiesImpl(new DescriptorFactory(),
                new DescriptorSaver(descriptorService));
        StaticPostgresqlDescriptorFacilitiesAccessor.setFacilities(facilities);
    }

    @Test
    void givenAnEntityHasNeitherIdNorUUID_whenItIsSaved_thenANewDescriptorShouldBeGeneratedWithNewObjectIdAndAssignedToUuidAndItsInternalIdAssignedToId() {
        when(descriptorService.createExternalId()).thenReturn("external-id");
        TestJpaModel model = new TestJpaModel();

        listener.fillRequiredFields(model);

        assertThat(model.getUuid(), is(notNullValue()));
        assertThat(model.getUuid().getExternalId(), is("external-id"));
        assertThat(model.getUuid().getInternalId(), is(not(internalId.toString())));
        assertThat(model.getId(), is(notNullValue()));
        assertThat(model.getId().toString(), is(equalTo(model.getUuid().getInternalId())));

        verify(descriptorService).store(model.getUuid());
    }

    @Test
    void givenAnEntityHasNoIdButHasUUID_whenItIsSaved_thenDescriptorShouldNotBeGeneratedButUUIDsInternalIdAssignedToId() {
        TestJpaModel model = new TestJpaModel();
        model.setUuid(descriptor);

        listener.fillRequiredFields(model);

        assertThat(model.getUuid(), is(sameInstance(descriptor)));
        assertThat(model.getId(), is(internalId));

        verify(descriptorService, never()).store(any());
    }

    @Test
    void givenAnEntityHasIdButNoUUID_whenItIsSaved_thenANewDescriptorShouldBeGeneratedForThatIdAndAssignedToUuid() {
        when(descriptorService.createExternalId()).thenReturn("external-id");
        TestJpaModel model = new TestJpaModel();
        model.setId(internalId);

        listener.fillRequiredFields(model);

        assertThat(model.getUuid(), is(notNullValue()));
        assertThat(model.getUuid().getExternalId(), is("external-id"));
        assertThat(model.getUuid().getInternalId(), is(internalId.toString()));
        assertThat(model.getId(), is(internalId));

        verify(descriptorService).store(model.getUuid());
    }

    @Test
    void givenAnEntityHasBothIdAndUUID_whenItIsSaved_thenNothingShouldHappen() {
        TestJpaModel model = new TestJpaModel();
        model.setId(internalId);
        model.setUuid(descriptor);

        listener.fillRequiredFields(model);

        assertThat(model.getUuid(), is(sameInstance(descriptor)));
        assertThat(model.getId(), is(internalId));

        verify(descriptorService, never()).store(any());
    }
}