package io.extremum.elasticsearch.springdata.repository;

import io.extremum.elasticsearch.model.ElasticsearchCommonModel;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.lucene.uid.Versions;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.seqno.SequenceNumbers;

import static io.extremum.elasticsearch.springdata.repository.ElasticsearchModels.asElasticsearchModel;

/**
 * @author rpuch
 */
public class SequenceNumberOperations {
    public void fillSequenceNumberAndPrimaryTermOnIndexRequest(Object object, IndexRequest indexRequest) {
        asElasticsearchModel(object).ifPresent(model -> {
            if (model.getSeqNo() != null) {
                indexRequest.setIfSeqNo(model.getSeqNo());
                resetVersionToMatchAnyBecauseItDoesNotWorkWithSeqNo(indexRequest);
            }
            if (model.getPrimaryTerm() != null) {
                indexRequest.setIfPrimaryTerm(model.getPrimaryTerm());
            }
        });
    }

    private void resetVersionToMatchAnyBecauseItDoesNotWorkWithSeqNo(IndexRequest indexRequest) {
        indexRequest.version(Versions.MATCH_ANY);
        indexRequest.versionType(VersionType.INTERNAL);
    }

    public void setSequenceNumberAndPrimaryTermAfterIndexing(Object indexedEntity,
            IndexResponse response) {
        if (indexedEntity == null) {
            return;
        }
        if (response.getSeqNo() == SequenceNumbers.UNASSIGNED_SEQ_NO
                && response.getPrimaryTerm() == SequenceNumbers.UNASSIGNED_PRIMARY_TERM) {
            return;
        }

        if (!(indexedEntity instanceof ElasticsearchCommonModel)) {
            return;
        }

        ElasticsearchCommonModel model = (ElasticsearchCommonModel) indexedEntity;

        model.setSeqNo(response.getSeqNo());
        model.setPrimaryTerm(response.getPrimaryTerm());
    }
}
