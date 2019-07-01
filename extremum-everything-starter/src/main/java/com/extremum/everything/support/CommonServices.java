package com.extremum.everything.support;

import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;

/**
 * @author rpuch
 */
public interface CommonServices {
    <M extends Model> CommonService<M> findServiceByModel(Class<? extends M> modelClass);
}
