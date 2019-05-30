package com.extremum.elasticsearch.dao;

import com.extremum.common.mapper.JsonObjectMapper;
import com.extremum.elasticsearch.TestWithServices;
import com.extremum.elasticsearch.model.TestElasticsearchModel;
import com.extremum.elasticsearch.properties.ElasticsearchProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Test
    void givenAnEntityWasSaved_whenGetThisEntityRawJson_thenItShouldBeDeserializedToTheSameValuesByOurObjectMapper()
            throws Exception {
        TestElasticsearchModel model = saveEntity();

        String json = getAsJson(TestElasticsearchModel.INDEX, model.getId())
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

    private Optional<String> getAsJson(String indexName, String id) {
        try (RestHighLevelClient client = getClient()) {
            GetResponse response = client.get(
                    new GetRequest(indexName, id),
                    RequestOptions.DEFAULT
            );

            if (response.isExists()) {
                return Optional.of(response.getSourceAsString());
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to get data by id " + id +
                    " from index " + indexName, e);
        }
    }

    private RestHighLevelClient getClient() {
        List<HttpHost> httpHosts = elasticsearchProperties.getHosts().stream()
                .map(h -> new HttpHost(h.getHost(), h.getPort(), h.getProtocol()))
                .collect(Collectors.toList());

        RestClientBuilder builder = RestClient.builder(httpHosts.toArray(new HttpHost[]{}));
        return new RestHighLevelClient(builder);
    }

    private TestElasticsearchModel parseJsonWithOurObjectMapper(String json) throws IOException {
        ObjectMapper mapper = JsonObjectMapper.createWithoutDescriptorTransfiguration();
        return mapper.readerFor(TestElasticsearchModel.class).readValue(json);
    }
}
