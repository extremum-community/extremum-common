package com.extremum.elasticsearch.dao.extractor;

import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.common.descriptor.service.DescriptorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.SearchHit;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SearchHitAccessorFacade extends AccessorFacade {
    private final SearchHit response;
    private final DescriptorService descriptorService;

    @Override
    public String getId() {
        return response.getId();
    }

    @Override
    public Descriptor getUuid() {
        return descriptorService.loadByInternalId(response.getId())
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Did not find a descriptor by id '%s'", response.getId())));
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
