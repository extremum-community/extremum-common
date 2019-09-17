package io.extremum.elasticsearch.model;

import io.extremum.common.model.annotation.HardDelete;
import io.extremum.common.model.annotation.ModelName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;

@ModelName("HardDeleteElasticsearchModel")
@Document(indexName = HardDeleteElasticsearchModel.INDEX)
@Getter @Setter
@HardDelete
public class HardDeleteElasticsearchModel extends ElasticsearchCommonModel {

    public static final String INDEX = "hard_delete_test_entities";

    private String name;

    public enum FIELDS {
        name
    }
}
