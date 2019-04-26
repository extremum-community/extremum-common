package com.extremum.common.dao.extractor;

import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.models.ElasticData;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FromSearchResponseExtracter extends Extractor {
    private SearchResponse response;

    public FromSearchResponseExtracter(SearchResponse response) {
        this.response = response;
    }

    @Override
    public ElasticData extract() {
        log.warn("Please, use extractAsList() method instead. Method extract() will return only one item or null is " +
                "search result is empty");
        final List<ElasticData> list = extractAsList();
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<ElasticData> extractAsList() {
        List<ElasticData> foundData = new ArrayList<>();

        for (SearchHit hit : response.getHits()) {
            final ElasticData.ElasticDataBuilder builder = ElasticData.builder();

            DescriptorService.loadByInternalId(hit.getId()).ifPresent(d -> {
                builder.uuid(d);
                builder.modelName(d.getModelType());
            });

            builder.id(hit.getId());
            builder.seqNo(hit.getSeqNo());
            builder.primaryTerm(hit.getPrimaryTerm());
            builder.rawDocument(hit.getSourceAsString());
            builder.version(hit.getVersion());

            populateFromSourceMap(hit.getSourceAsMap(), builder);

            foundData.add(builder.build());
        }

        return foundData;
    }
}
