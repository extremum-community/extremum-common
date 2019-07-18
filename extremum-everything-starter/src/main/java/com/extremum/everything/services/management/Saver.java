package com.extremum.everything.services.management;

import com.extremum.common.models.Model;

/**
 * @author rpuch
 */
public interface Saver<M extends Model> {
    M save(M model);
}
