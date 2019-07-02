package com.extremum.common.descriptor.service;

/**
 * @author rpuch
 */
public class StaticDescriptorServiceAccessor {
    private static volatile DescriptorService SERVICE_INSTANCE;

    public static DescriptorService getDescriptorService() {
        return SERVICE_INSTANCE;
    }

    public static void setDescriptorService(DescriptorService descriptorService) {
        SERVICE_INSTANCE = descriptorService;
    }
}
