package com.extremum.common.descriptor.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class StaticDescriptorServiceAccessorTest {
    @Test
    void test() {
        DescriptorService descriptorService = Mockito.mock(DescriptorService.class);
        StaticDescriptorServiceAccessor.setDescriptorService(descriptorService);

        assertThat(StaticDescriptorServiceAccessor.getDescriptorService(), is(sameInstance(descriptorService)));
    }
}