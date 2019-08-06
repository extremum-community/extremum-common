package com.extremum.sharedmodels.descriptor;

/**
 * @author rpuch
 */
public class StaticDescriptorLoaderAccessor {
    private static volatile DescriptorLoader LOADER_INSTANCE;

    /**
     *
     * @deprecated Do not every use this in your code! This is just an ugly thing we have to use
     * in our infrastructure for descriptors
     */
    @Deprecated
    public static DescriptorLoader getDescriptorLoader() {
        return LOADER_INSTANCE;
    }

    public static void setDescriptorLoader(DescriptorLoader descriptorLoader) {
        LOADER_INSTANCE = descriptorLoader;
    }
}
