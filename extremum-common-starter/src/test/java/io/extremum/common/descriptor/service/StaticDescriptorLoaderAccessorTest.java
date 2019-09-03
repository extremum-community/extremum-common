package io.extremum.common.descriptor.service;

import io.extremum.sharedmodels.descriptor.DescriptorLoader;
import io.extremum.sharedmodels.descriptor.StaticDescriptorLoaderAccessor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class StaticDescriptorLoaderAccessorTest {
    @Test
    void test() {
        DescriptorLoader descriptorLoader = Mockito.mock(DescriptorLoader.class);
        StaticDescriptorLoaderAccessor.setDescriptorLoader(descriptorLoader);

        assertThat(StaticDescriptorLoaderAccessor.getDescriptorLoader(), is(sameInstance(descriptorLoader)));
    }
}