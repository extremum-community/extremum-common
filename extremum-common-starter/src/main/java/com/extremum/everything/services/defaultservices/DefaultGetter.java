package com.extremum.everything.services.defaultservices;

import com.extremum.common.models.Model;
import com.extremum.everything.services.management.Getter;

/**
 * @author rpuch
 */
public interface DefaultGetter<M extends Model> extends Getter<M> {
    M get(String id);
}
