package io.extremum.dynamic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.caching.impl.CachingGithubNetworkntSchemaProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicModelHeater implements ApplicationListener<ContextRefreshedEvent> {
    private final CachingGithubNetworkntSchemaProvider schemaProvider;
    private final GithubSchemaProperties githubSchemaProperties;

    private volatile boolean alreadyHeated = false;
    private final SchemaMetaService schemaMetaService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadyHeated) {
            log.info("Already heated");
        } else {
            alreadyHeated = true;

            String schemaName = githubSchemaProperties.getSchemaName();
            if (schemaName == null) {
                log.warn("Schema name does not provided; DynamicModelHeater can't warm up a dynamic model system");
            } else {
                NetworkntSchema loaded = schemaProvider.loadSchema(schemaName);
                JsonNode title = loaded.getSchema().getSchemaNode().get("title");
                if (!isTextNode(title)) {
                    log.warn("No 'title' attribute found in schema {}. Model name for that schema can't be registered " +
                            "in a descriptor determinator", loaded.getSchema());
                } else {
                    schemaMetaService.registerMapping(title.textValue(), schemaName);
                }
            }
        }
    }

    private boolean isTextNode(JsonNode title) {
        return title instanceof TextNode;
    }
}
