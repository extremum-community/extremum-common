package com.extremum.starter;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
public interface SystemMapperModulesSupplier extends Function<ObjectMapper, List<Module>> {
}
