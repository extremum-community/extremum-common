package com.extremum.starter;

import com.fasterxml.jackson.databind.Module;

import java.util.List;
import java.util.function.Supplier;

@FunctionalInterface
public interface RedisMapperModulesSupplier extends Supplier<List<Module>> {
}
