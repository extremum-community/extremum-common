package com.extremum.everything.services.defaultservices;

import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;
import com.extremum.everything.support.CommonServices;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultSaverImpl<M extends Model> implements DefaultSaver<M> {
    private final CommonServices commonServices;

    @Override
    public M save(M model) {
        CommonService<M> service = (CommonService<M>) commonServices.findServiceByModel(model.getClass());
        return service.save(model);
    }
}
