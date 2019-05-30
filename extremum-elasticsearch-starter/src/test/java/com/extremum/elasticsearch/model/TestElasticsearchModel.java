package com.extremum.elasticsearch.model;

import com.extremum.common.models.annotation.ModelName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;

@ModelName("TestElasticsearchModel")
@Document(indexName = "test_entities")
@Getter @Setter
public class TestElasticsearchModel extends ElasticsearchCommonModel {

    public static final String INDEX = "test_entities";

    private String name;

    public enum FIELDS {
        name
    }
}
