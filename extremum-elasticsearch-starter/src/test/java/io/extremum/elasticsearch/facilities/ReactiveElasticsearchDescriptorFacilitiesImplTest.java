package io.extremum.elasticsearch.facilities;

import io.extremum.common.descriptor.factory.ReactiveDescriptorSaver;
import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.descriptor.service.ReactiveDescriptorService;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactiveElasticsearchDescriptorFacilitiesImplTest {
    private ReactiveElasticsearchDescriptorFacilitiesImpl facilities;

    @Spy
    private DescriptorService descriptorService = new InMemoryDescriptorService();
    @Spy
    private ReactiveDescriptorService reactiveDescriptorService = new InMemoryReactiveDescriptorService();

    private final UUID uuid = UUID.randomUUID();

    @BeforeEach
    void initDescriptorSaver() {
        ReactiveDescriptorSaver descriptorSaver = new ReactiveDescriptorSaver(
                descriptorService, reactiveDescriptorService);
        facilities = new ReactiveElasticsearchDescriptorFacilitiesImpl(descriptorSaver);
    }

    @Test
    void whenCreatingANewDescriptorWithANewInternalId_thenARandomObjectIdShouldBeGeneratedAndDescriptorSavedWithThatId() {
        when(descriptorService.createExternalId()).thenReturn("external-id");

        Descriptor descriptor = facilities.create(uuid, "Test").block();

        assertThat(descriptor, is(notNullValue()));
        assertThat(descriptor.getExternalId(), is("external-id"));
        assertThat(descriptor.getInternalId(), is(equalTo(uuid.toString())));
        assertThat(descriptor.getModelType(), is("Test"));
        assertThat(descriptor.getStorageType(), is(Descriptor.StorageType.ELASTICSEARCH));

        //noinspection UnassignedFluxMonoInstance
        verify(reactiveDescriptorService).store(descriptor);
    }

    @Test
    void givenDescriptorIsForElasticsearch_whenResolvingADescriptor_thenInternalIdShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .internalId(uuid.toString())
                .storageType(Descriptor.StorageType.ELASTICSEARCH)
                .build();

        UUID resolvedId = facilities.resolve(descriptor).block();

        assertThat(resolvedId, is(equalTo(uuid)));
    }

    @Test
    void givenDescriptorIsNotForElasticsearch_whenResolvingADescriptor_thenInternalIdShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .internalId(uuid.toString())
                .storageType(Descriptor.StorageType.POSTGRES)
                .build();

        Mono<UUID> mono = facilities.resolve(descriptor);
        try {
            mono.block();
            fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Wrong descriptor storage type POSTGRES"));
        }
    }
}