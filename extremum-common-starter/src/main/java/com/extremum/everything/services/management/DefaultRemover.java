package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.everything.services.Remover;

/**
 * @author rpuch
 */
public interface DefaultRemover<M extends Model> extends Remover {
    void remove(String id);
}
