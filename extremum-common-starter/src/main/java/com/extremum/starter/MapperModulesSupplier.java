package com.extremum.starter;

import com.extremum.common.descriptor.service.DescriptorService;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@FunctionalInterface
public interface MapperModulesSupplier {
    List<Module> makeModules(ObjectMapper mapper, DescriptorService descriptorService);
}
