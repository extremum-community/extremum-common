package com.extremum.common.cofnig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.http.client.HttpClient;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElasticDaoConfig {
    private List<String> elasticUrlList;
    private String searchPrefix = "_search?size=";

    private String scrollKeepAlive = "30s";
    private String scrollSize = "10";
    private int maxRetries = 3;
    private String endpoint;
    private HttpClient client;
}
