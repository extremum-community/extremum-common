package com.extremum.elasticsearch.model;

import com.extremum.common.models.annotation.ModelName;
import lombok.Getter;
import lombok.Setter;

@ModelName("TestElasticModel")
@Getter @Setter
public class TestElasticModel extends ElasticCommonModel {

    public static final String INDEX = "test_entities";

    private String name;

    public enum FIELDS {
        name
    }
}
