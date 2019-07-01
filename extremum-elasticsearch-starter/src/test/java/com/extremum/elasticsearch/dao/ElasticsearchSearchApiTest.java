package com.extremum.elasticsearch.dao;

import com.extremum.elasticsearch.TestWithServices;
import com.extremum.elasticsearch.model.TestElasticsearchModel;
import com.extremum.elasticsearch.properties.ElasticsearchProperties;
import com.extremum.elasticsearch.service.TestElasticsearchModelService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
@SpringBootTest(classes = RepositoryBasedElasticsearchDaoConfiguration.class)
class ElasticsearchSearchApiTest extends TestWithServices {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TestElasticsearchModelDao dao;
    @Autowired
    private ElasticsearchProperties elasticsearchProperties;

    TestElasticsearchClient client;

    @BeforeEach
    void initClient() {
        client = new TestElasticsearchClient(elasticsearchProperties);
    }

    @Test
    void test() {
        TestElasticsearchModel model1 = modelWithName("abc-def");
        TestElasticsearchModel model2 = modelWithName("def-abc");
        TestElasticsearchModel model3 = modelWithName("abc-def-bar");

        dao.saveAll(Arrays.asList(model1, model2, model3));

        QueryStringQueryBuilder query = QueryBuilders.queryStringQuery("abc-def").analyzer("keyword");
        SearchRequest request = new SearchRequest(TestElasticsearchModel.INDEX)
                .source(new SearchSourceBuilder().query(query));
        SearchResponse response = client.search(request);

        assertThat(response.getHits().getTotalHits().value, is(1L));
    }

    @NotNull
    private TestElasticsearchModel modelWithName(String name) {
        TestElasticsearchModel model1 = new TestElasticsearchModel();
        model1.setName(name);
        return model1;
    }
}
