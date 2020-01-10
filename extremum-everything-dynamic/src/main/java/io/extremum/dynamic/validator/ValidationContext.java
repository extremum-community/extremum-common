package io.extremum.dynamic.validator;

import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class ValidationContext {
    private final Set<String> paths;

    public Set<String> getPaths() {
        return paths;
    }
}
