package com.extremum.common.descriptor.service;

import com.extremum.common.descriptor.dao.DescriptorDao;
import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
public class DescriptorServiceConfigurator {
    private final DescriptorService descriptorService;

    @PostConstruct
    public void init() {
        DescriptorServiceImpl.setInstance(descriptorService);
    }

}
