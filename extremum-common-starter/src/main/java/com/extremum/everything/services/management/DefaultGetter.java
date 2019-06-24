package com.extremum.everything.services.management;

import com.extremum.common.models.Model;

/**
 * @author rpuch
 */
public interface DefaultGetter<M extends Model> extends Getter<M> {
    M get(String id);
}
