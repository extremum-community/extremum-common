package com.extremum.elasticsearch.dao;

import com.extremum.common.mapper.JsonObjectMapper;
import com.extremum.elasticsearch.TestWithServices;
import com.extremum.elasticsearch.model.TestElasticsearchModel;
import com.extremum.elasticsearch.properties.ElasticsearchProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
@SpringBootTest(classes = ElasticsearchCommonDaoConfiguration.class)
class ElasticsearchStorageStabilityTests extends TestWithServices {
    @Autowired
    private ElasticsearchProperties elasticsearchProperties;
    @Autowired
    private TestElasticsearchModelDao dao;

    private TestElasticsearchClient client;

    @BeforeEach
    void createClient() {
        client = new TestElasticsearchClient(elasticsearchProperties);
    }

    @Test
    void givenAnEntityWasSaved_whenGetThisEntityRawJson_thenItShouldBeDeserializedToTheSameValuesByOurObjectMapper()
            throws Exception {
        TestElasticsearchModel model = saveEntity();

        String json = client.getAsJson(TestElasticsearchModel.INDEX, model.getId())
                .orElseThrow(() -> new IllegalArgumentException("Did not find anything"));

        TestElasticsearchModel parsedModel = parseJsonWithOurObjectMapper(json);

        assertThat(parsedModel.getId(), is(notNullValue()));
        assertThat(parsedModel.getUuid(), is(notNullValue()));
        assertThat(parsedModel.getCreated(), is(notNullValue()));
        assertThat(parsedModel.getModified(), is(notNullValue()));
        assertThat(parsedModel.getDeleted(), is(false));
        assertThat(parsedModel.getVersion(), is(nullValue()));
        assertThat(parsedModel.getPrimaryTerm(), is(nullValue()));
        assertThat(parsedModel.getSeqNo(), is(nullValue()));
        assertThat(parsedModel.getName(), is("test"));
    }

    @NotNull
    private TestElasticsearchModel saveEntity() {
        TestElasticsearchModel model = new TestElasticsearchModel();
        model.setName("test");

        dao.save(model);
        return model;
    }

    private TestElasticsearchModel parseJsonWithOurObjectMapper(String json) throws IOException {
        ObjectMapper mapper = JsonObjectMapper.createWithoutDescriptorTransfiguration();
        return mapper.readerFor(TestElasticsearchModel.class).readValue(json);
    }
}
