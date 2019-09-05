package io.extremum.common.descriptor.service;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.DescriptorLoader;
import io.extremum.sharedmodels.descriptor.DescriptorNotFoundException;
import io.extremum.sharedmodels.descriptor.StaticDescriptorLoaderAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DescriptorTest {
    private DescriptorLoader oldDescriptorLoader;

    @Mock
    private DescriptorLoader descriptorLoader;

    private final Descriptor descriptorInDb = Descriptor.builder()
            .externalId("external-id")
            .internalId("internal-id")
            .storageType(Descriptor.StorageType.MONGO)
            .build();

    @BeforeEach
    void initDescriptorLoader() {
        //noinspection deprecation
        oldDescriptorLoader = StaticDescriptorLoaderAccessor.getDescriptorLoader();
        StaticDescriptorLoaderAccessor.setDescriptorLoader(descriptorLoader);
    }

    @AfterEach
    void restoreDescriptorLoader() {
        StaticDescriptorLoaderAccessor.setDescriptorLoader(oldDescriptorLoader);
    }

    @Test
    void givenInternalIdIsNotNull_whenGetInternalIdReactivelyIsUsed_thenTheInternalIdShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .internalId("internal-id")
                .build();

        Mono<String> internalIdMono = descriptor.getInternalIdReactively();

        StepVerifier.create(internalIdMono)
                .expectNext("internal-id")
                .verifyComplete();
    }

    @Test
    void givenInternalIdIsNull_whenGetInternalIdReactivelyIsUsed_thenTheInternalIdShouldBeFilledAndReturned() {
        when(descriptorLoader.loadByExternalIdReactively("external-id"))
                .thenReturn(Mono.just(descriptorInDb));

        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .build();

        Mono<String> internalIdMono = descriptor.getInternalIdReactively();

        StepVerifier.create(internalIdMono)
                .expectNext("internal-id")
                .verifyComplete();

        assertThat(descriptor.getInternalId(), is("internal-id"));
    }

    @Test
    void givenInternalIdIsNullAndNoDescriptorExistsInDB_whenGetInternalIdReactivelyIsUsed_thenAnErrorShouldBeReturned() {
        when(descriptorLoader.loadByExternalIdReactively(anyString()))
                .thenReturn(Mono.empty());

        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .build();

        Mono<String> internalIdMono = descriptor.getInternalIdReactively();

        StepVerifier.create(internalIdMono)
                .expectErrorSatisfies(ex -> {
                    assertThat(ex, instanceOf(DescriptorNotFoundException.class));
                    assertThat(ex.getMessage(), is("Internal ID was not found for external ID external-id"));
                })
                .verify();
    }

    @Test
    void givenExternalIdIsNotNull_whenGetExternalIdReactivelyIsUsed_thenTheExternalIdShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .build();

        Mono<String> externalIdMono = descriptor.getExternalIdReactively();

        StepVerifier.create(externalIdMono)
                .expectNext("external-id")
                .verifyComplete();
    }

    @Test
    void givenExternalIdIsNull_whenGetExternalIdReactivelyIsUsed_thenTheExternalIdShouldBeFilledAndReturned() {
        when(descriptorLoader.loadByInternalIdReactively("internal-id"))
                .thenReturn(Mono.just(descriptorInDb));

        Descriptor descriptor = Descriptor.builder()
                .internalId("internal-id")
                .build();

        Mono<String> externalIdMono = descriptor.getExternalIdReactively();

        StepVerifier.create(externalIdMono)
                .expectNext("external-id")
                .verifyComplete();

        assertThat(descriptor.getExternalId(), is("external-id"));
    }

    @Test
    void givenExternalIdIsNullAndNoDescriptorExistsInDB_whenGetExternalIdReactivelyIsUsed_thenErrorShouldBeReturned() {
        when(descriptorLoader.loadByInternalIdReactively(anyString()))
                .thenReturn(Mono.empty());

        Descriptor descriptor = Descriptor.builder()
                .internalId("internal-id")
                .build();

        Mono<String> externalIdMono = descriptor.getExternalIdReactively();

        StepVerifier.create(externalIdMono)
                .expectErrorSatisfies(ex -> {
                    assertThat(ex, instanceOf(DescriptorNotFoundException.class));
                    assertThat(ex.getMessage(), is("Internal id internal-id without corresponding descriptor"));
                })
                .verify();
    }

    @Test
    void givenStorageTypeIsNotNull_whenGetStorageTypeReactivelyIsUsed_thenTheStorageTypeShouldBeReturned() {
        Descriptor descriptor = Descriptor.builder()
                .storageType(Descriptor.StorageType.MONGO)
                .build();

        Mono<Descriptor.StorageType> storageTypeMono = descriptor.getStorageTypeReactively();

        StepVerifier.create(storageTypeMono)
                .expectNext(Descriptor.StorageType.MONGO)
                .verifyComplete();
    }

    @Test
    void givenStorageTypeIsNullAndExternalIdIsNotNull_whenGetStorageTypeReactivelyIsUsed_thenTheStorageTypeShouldBeReturnedAndFilled() {
        when(descriptorLoader.loadByExternalIdReactively("external-id"))
                .thenReturn(Mono.just(descriptorInDb));

        Descriptor descriptor = Descriptor.builder()
                .externalId("external-id")
                .build();

        Mono<Descriptor.StorageType> storageTypeMono = descriptor.getStorageTypeReactively();

        StepVerifier.create(storageTypeMono)
                .expectNext(Descriptor.StorageType.MONGO)
                .verifyComplete();

        assertThat(descriptor.getStorageType(), is(Descriptor.StorageType.MONGO));
    }

    @Test
    void givenStorageTypeIsNullAndInternalIdIsNotNull_whenGetStorageTypeReactivelyIsUsed_thenTheStorageTypeShouldBeReturnedAndFilled() {
        when(descriptorLoader.loadByInternalIdReactively("internal-id"))
                .thenReturn(Mono.just(descriptorInDb));

        Descriptor descriptor = Descriptor.builder()
                .internalId("internal-id")
                .build();

        Mono<Descriptor.StorageType> storageTypeMono = descriptor.getStorageTypeReactively();

        StepVerifier.create(storageTypeMono)
                .expectNext(Descriptor.StorageType.MONGO)
                .verifyComplete();

        assertThat(descriptor.getStorageType(), is(Descriptor.StorageType.MONGO));
    }
}
