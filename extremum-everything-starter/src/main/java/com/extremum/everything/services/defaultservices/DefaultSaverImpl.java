package com.extremum.everything.services.defaultservices;

import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;
import com.extremum.everything.support.CommonServices;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultSaverImpl implements DefaultSaver {
    private final CommonServices commonServices;

    @Override
    public Model save(Model model) {
        CommonService<Model> service = commonServices.findServiceByModel(model.getClass());
        return service.save(model);
    }
}
