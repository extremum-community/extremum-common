package com.extremum.elasticsearch.repositories;

import com.extremum.common.utils.DateUtils;
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

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rpuch
 */
public class SoftDeleteElasticsearchRepository<T extends ElasticsearchCommonModel> extends BaseElasticsearchRepository<T> {
    private static final String PAINLESS_LANGUAGE = "painless";

    private static final String MODIFIED = ElasticsearchCommonModel.FIELDS.modified.name();

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
    public boolean patch(String id, String painlessScript, Map<String, Object> scriptParams) {
        UpdateRequest updateRequest = new UpdateRequest(metadata.getIndexName(), id);
        Script script = createScript(painlessScript, scriptParams);
        updateRequest.script(script);

        UpdateQuery updateQuery = new UpdateQueryBuilder()
                .withClass(metadata.getJavaType())
                .withId(id)
                .withUpdateRequest(updateRequest)
                .build();

        UpdateResponse response = elasticsearchOperations.update(updateQuery);
        return response.getResult() == DocWriteResponse.Result.UPDATED;
    }

    private Script createScript(String painlessScript, Map<String, Object> params) {
        String scriptWithModificationTimeChange = amendWithModificationTimeChange(painlessScript);
        Map<String, Object> paramsWithModificationTimeChange = amendWithModificationTime(params);
        return new Script(ScriptType.INLINE, PAINLESS_LANGUAGE, scriptWithModificationTimeChange,
                paramsWithModificationTimeChange);
    }

    private String amendWithModificationTimeChange(String painlessScript) {
        return painlessScript + changeModificationTimeScriptSuffix();
    }

    private String changeModificationTimeScriptSuffix() {
        return "; ctx._source." + MODIFIED + " = params." + MODIFIED;
    }

    private Map<String, Object> amendWithModificationTime(Map<String, Object> params) {
        Map<String, Object> paramsWithModificationTimeChange = new HashMap<>(params);
        paramsWithModificationTimeChange.put(MODIFIED, getNowAsString());
        return paramsWithModificationTimeChange;
    }

    private String getNowAsString() {
        return DateUtils.formatZonedDateTimeISO_8601(ZonedDateTime.now());
    }
}
