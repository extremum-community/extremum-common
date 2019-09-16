package io.extremum.elasticsearch.springdata.reactiverepository;

import io.extremum.common.utils.DateUtils;
import io.extremum.elasticsearch.dao.ReactiveElasticsearchCommonDao;
import io.extremum.elasticsearch.dao.SearchOptions;
import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import io.extremum.elasticsearch.springdata.repository.UpdateFailedException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQueryBuilder;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.data.elasticsearch.repository.support.SimpleReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rpuch
 */
abstract class BaseReactiveElasticsearchRepository<T extends ElasticsearchCommonModel>
        extends SimpleReactiveElasticsearchRepository<T, String>
        implements ReactiveElasticsearchCommonDao<T> {
    private static final String PAINLESS_LANGUAGE = "painless";

    private static final String MODIFIED = ElasticsearchCommonModel.FIELDS.modified.name();

    private static final String ANALYZER_KEYWORD = "keyword";

    private final ElasticsearchEntityInformation<T, String> metadata;
    private ReactiveElasticsearchAdditionalOperations additionalOperations;

    BaseReactiveElasticsearchRepository(ElasticsearchEntityInformation<T, String> metadata,
                                        ReactiveElasticsearchOperations elasticsearchOperations) {
        super(metadata, elasticsearchOperations);

        this.metadata = metadata;
    }

    public void setAdditionalOperations(ReactiveElasticsearchAdditionalOperations additionalOperations) {
        this.additionalOperations = additionalOperations;
    }

    @Override
    public final Flux<T> findAll() {
        throw new UnsupportedOperationException("" +
                "Please do not call this method as a list of all documents may be very large");
    }

    @Override
    public final Flux<T> findAll(Sort sort) {
        throw new UnsupportedOperationException("" +
                "Please do not call this method as a list of all documents may be very large");
    }

    @Override
    public final Mono<Void> deleteAll() {
        throw new UnsupportedOperationException("We don't allow to delete all the documents in one go");
    }

    @Override
    public Flux<T> search(String queryString, SearchOptions searchOptions) {
        QueryStringQueryBuilder query = QueryBuilders.queryStringQuery(queryString);
        if (searchOptions.isExactFieldValueMatch()) {
            query.analyzer(ANALYZER_KEYWORD);
        }

        return search(query);
    }

    protected Flux<T> search(QueryBuilder query) {
        SearchQuery searchQuery = (new NativeSearchQueryBuilder()).withQuery(query).build();
        return additionalOperations.queryForPage(searchQuery, metadata.getJavaType());
    }

    @Override
    public Mono<Boolean> patch(String id, String painlessScript) {
        return patch(id, painlessScript, Collections.emptyMap());
    }

    @Override
    public Mono<Boolean> patch(String id, String painlessScript, Map<String, Object> scriptParams) {
        UpdateRequest updateRequest = new UpdateRequest(metadata.getIndexName(), id);
        Script script = createScript(painlessScript, scriptParams);
        updateRequest.script(script);

        UpdateQuery updateQuery = new UpdateQueryBuilder()
                .withClass(metadata.getJavaType())
                .withId(id)
                .withUpdateRequest(updateRequest)
                .build();

        return additionalOperations.update(updateQuery)
                .doOnNext(this::throwIfUpdateIsNotApplied)
                .thenReturn(true);
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

    private void throwIfUpdateIsNotApplied(UpdateResponse updateResponse) {
        if (updateResponse.getResult() != DocWriteResponse.Result.UPDATED) {
            throw new UpdateFailedException("Update result is not UPDATED but " + updateResponse.getResult());
        }
    }
}
