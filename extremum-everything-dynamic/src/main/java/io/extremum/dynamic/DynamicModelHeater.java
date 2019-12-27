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
    private final ReactiveDescriptorDeterminator descriptorDeterminator;
    private final GithubSchemaProperties githubSchemaProperties;

    private boolean alreadyHeated = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!alreadyHeated) {
            alreadyHeated = true;

            String schemaName = githubSchemaProperties.getSchemaName();
            if (schemaName != null) {
                NetworkntSchema loaded = schemaProvider.loadSchema(schemaName);
                if (descriptorDeterminator instanceof DefaultReactiveDescriptorDeterminator) {
                    JsonNode title = loaded.getSchema().getSchemaNode().get("title");
                    if (title instanceof TextNode) {
                        ((DefaultReactiveDescriptorDeterminator) descriptorDeterminator)
                                .registerDynamicModel(title.textValue());
                    } else {
                        log.warn("No 'title' attribute found in schema {}. Model name for that schema can't be registered " +
                                "in a descriptor determinator", loaded.getSchema());
                    }
                }
            } else {
                log.warn("Schema name does not provided; DynamicModelHeater can't warm up a dynamic model system");
            }
        } else {
            log.info("Already heated");
        }
    }
}
