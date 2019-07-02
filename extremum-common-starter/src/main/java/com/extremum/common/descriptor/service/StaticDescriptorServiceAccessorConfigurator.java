package com.extremum.common.descriptor.service;

import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
public class StaticDescriptorServiceAccessorConfigurator {
    private final DescriptorService descriptorService;

    @PostConstruct
    public void init() {
        StaticDescriptorServiceAccessor.setDescriptorService(descriptorService);
    }

}
