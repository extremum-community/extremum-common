package com.extremum.common.dao.extractor;

import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.models.ElasticData;
import org.elasticsearch.action.get.GetResponse;

import java.util.Collections;
import java.util.List;

public class FromGetResponseExtractor extends Extractor {
    private GetResponse response;

    public FromGetResponseExtractor(GetResponse response) {
        this.response = response;
    }

    @Override
    public ElasticData extract() {
        final ElasticData.ElasticDataBuilder builder = ElasticData.builder();

        DescriptorService.loadByInternalId(response.getId()).ifPresent(d -> {
            builder.uuid(d);
            builder.modelName(d.getModelType());
        });

        builder.id(response.getId());
        builder.seqNo(response.getSeqNo());
        builder.primaryTerm(response.getPrimaryTerm());
        builder.rawDocument(response.getSourceAsString());
        builder.version(response.getVersion());

        populateFromSourceMap(response.getSourceAsMap(), builder);

        return builder.build();
    }

    @Override
    public List<ElasticData> extractAsList() {
        return Collections.singletonList(extract());
    }
}
