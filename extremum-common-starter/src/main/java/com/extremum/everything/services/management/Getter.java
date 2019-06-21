package com.extremum.everything.services.management;

import com.extremum.common.models.Model;

/**
 * @author rpuch
 */
public interface Getter<M extends Model> {
    M get(String id);
}
