package com.extremum.common.descriptor.service;

/**
 * @author rpuch
 */
public class StaticDescriptorLoaderAccessor {
    private static volatile DescriptorLoader LOADER_INSTANCE;

    public static DescriptorLoader getDescriptorLoader() {
        return LOADER_INSTANCE;
    }

    public static void setDescriptorLoader(DescriptorLoader descriptorLoader) {
        LOADER_INSTANCE = descriptorLoader;
    }
}
