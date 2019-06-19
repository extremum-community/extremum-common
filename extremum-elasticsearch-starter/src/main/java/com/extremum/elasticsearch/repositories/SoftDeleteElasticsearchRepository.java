package com.extremum.elasticsearch.repositories;

import com.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQueryBuilder;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author rpuch
 */
public class SoftDeleteElasticsearchRepository<T extends ElasticsearchCommonModel> extends BaseElasticsearchRepository<T> {
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchEntityInformation<T, String> metadata;

    public SoftDeleteElasticsearchRepository(
            ElasticsearchEntityInformation<T, String> metadata,
            ElasticsearchOperations elasticsearchOperations) {
        super(metadata, elasticsearchOperations);

        this.elasticsearchOperations = elasticsearchOperations;
        this.metadata = metadata;
    }

    @Override
    public List<T> search(String queryString) {
        Iterable<T> results = search(QueryBuilders.queryStringQuery(queryString));
        return iterableToList(results);
    }

    @Override
    public boolean patch(String id, String painlessScript) {
        return patch(id, painlessScript, Collections.emptyMap());
    }

    @Override
    public boolean patch(String id, String painlessScript, Map<String, Object> params) {
        UpdateRequest updateRequest = new UpdateRequest(metadata.getIndexName(), id);
        updateRequest.script(new Script(ScriptType.INLINE, "painless", painlessScript, params));

        UpdateQuery updateQuery = new UpdateQueryBuilder()
                .withClass(metadata.getJavaType())
                .withId(id)
                .withUpdateRequest(updateRequest)
                .build();

        UpdateResponse response = elasticsearchOperations.update(updateQuery);
        return response.getResult() == DocWriteResponse.Result.UPDATED;
    }
}
