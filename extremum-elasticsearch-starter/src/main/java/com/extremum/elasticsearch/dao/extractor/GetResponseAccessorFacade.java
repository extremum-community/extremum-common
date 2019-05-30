package com.extremum.elasticsearch.dao.extractor;

import com.extremum.common.descriptor.Descriptor;
import com.extremum.common.descriptor.service.DescriptorService;
import org.elasticsearch.action.get.GetResponse;

import java.util.Map;

public class GetResponseAccessorFacade extends AccessorFacade {
    private GetResponse response;

    public GetResponseAccessorFacade(GetResponse response) {
        this.response = response;
    }

    @Override
    public String getId() {
        return response.getId();
    }

    @Override
    public Descriptor getUuid() {
        return DescriptorService.loadByInternalId(response.getId()).get();
    }

    @Override
    public Long getVersion() {
        return response.getVersion();
    }

    @Override
    public String getRawSource() {
        return response.getSourceAsString();
    }

    @Override
    public Long getSeqNo() {
        return response.getSeqNo();
    }

    @Override
    public Long getPrimaryTerm() {
        return response.getPrimaryTerm();
    }

    @Override
    public Map<String, Object> getSourceAsMap() {
        return response.getSourceAsMap();
    }
}
