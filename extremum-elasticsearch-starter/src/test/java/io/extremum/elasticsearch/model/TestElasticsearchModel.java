package io.extremum.elasticsearch.model;

import io.extremum.common.model.annotation.ModelName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;

@ModelName("TestElasticsearchModel")
@Document(indexName = TestElasticsearchModel.INDEX)
@Getter
@Setter
public class TestElasticsearchModel extends ElasticsearchCommonModel {

    public static final String INDEX = "test_entities";

    private String name;

    public enum FIELDS {
        name
    }
}
