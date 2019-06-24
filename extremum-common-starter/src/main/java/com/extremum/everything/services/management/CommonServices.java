package com.extremum.everything.services.management;

import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;

import java.io.Serializable;

/**
 * @author rpuch
 */
public interface CommonServices {
    <ID extends Serializable, M extends Model> CommonService<ID, M> findServiceByModel(
            Class<? extends M> modelClass);
}
