package com.extremum.common.descriptor.service;

import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
public class StaticDescriptorLoaderAccessorConfigurator {
    private final DescriptorLoader descriptorLoader;

    @PostConstruct
    public void init() {
        StaticDescriptorLoaderAccessor.setDescriptorLoader(descriptorLoader);
    }

}
