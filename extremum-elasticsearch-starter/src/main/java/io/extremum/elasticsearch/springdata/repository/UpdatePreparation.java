package io.extremum.elasticsearch.springdata.repository;

import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.update.UpdateRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.util.Assert;

import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class UpdatePreparation {
    private final ElasticsearchOperations elasticsearchOperations;

    public UpdateRequest prepareUpdate(UpdateQuery query) {
        String indexName = hasText(query.getIndexName()) ? query.getIndexName()
                : elasticsearchOperations.getPersistentEntityFor(query.getClazz()).getIndexName();
        String type = hasText(query.getType()) ? query.getType() :
                elasticsearchOperations.getPersistentEntityFor(query.getClazz()).getIndexType();
        Assert.notNull(indexName, "No index defined for Query");
        Assert.notNull(type, "No type define for Query");
        Assert.notNull(query.getId(), "No Id define for Query");
        Assert.notNull(query.getUpdateRequest(), "No IndexRequest define for Query");
        UpdateRequest updateRequest = new UpdateRequest(indexName, type, query.getId());
        updateRequest.routing(query.getUpdateRequest().routing());

        if (query.getUpdateRequest().script() == null) {
            // doc
            if (query.DoUpsert()) {
                updateRequest.docAsUpsert(true).doc(query.getUpdateRequest().doc());
            } else {
                updateRequest.doc(query.getUpdateRequest().doc());
            }
        } else {
            // or script
            updateRequest.script(query.getUpdateRequest().script());
        }

        return updateRequest;
    }
}
