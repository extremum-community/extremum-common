package com.extremum.elasticsearch.dao;

import lombok.Builder;
import lombok.Getter;

/**
 * @author rpuch
 */
@Builder
@Getter
public class SearchOptions {
    private final boolean exactFieldValueMatch;
}
