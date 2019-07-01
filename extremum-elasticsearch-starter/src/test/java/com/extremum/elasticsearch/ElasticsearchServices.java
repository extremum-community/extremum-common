package com.extremum.elasticsearch;

import com.extremum.testcontainers.CoreServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

/**
 * @author rpuch
 */
class ElasticsearchServices extends CoreServices {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchServices.class);

    static {
        startElasticsearchIfNeeded();
    }

    private static void startElasticsearchIfNeeded() {
        workaroundElasticsearchClientStartupQuirk();

        if (shouldStartOurOwnElasticsearch()) {
            startElasticsearch();
        } else {
            setElasticsearchEndpointProperties("localhost", 9200);
        }
    }

    private static void workaroundElasticsearchClientStartupQuirk() {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    private static boolean shouldStartOurOwnElasticsearch() {
        return "true".equals(System.getProperty("start.elasticsearch", "true"));
    }

    private static void startElasticsearch() {
        ElasticsearchContainer elasticSearch = new ElasticsearchContainer("elasticsearch:7.1.0");
        elasticSearch.start();

        setElasticsearchEndpointProperties(elasticSearch.getContainerIpAddress(), elasticSearch.getFirstMappedPort());

        LOGGER.info("Elasticsearch host:port are {}:{}",
                elasticSearch.getContainerIpAddress(), elasticSearch.getFirstMappedPort());
    }

    private static void setElasticsearchEndpointProperties(String host, Integer port) {
        System.setProperty("elasticsearch.hosts[0].host", host);
        System.setProperty("elasticsearch.hosts[0].port", Integer.toString(port));
        System.setProperty("elasticsearch.hosts[0].protocol", "http");
    }

}
