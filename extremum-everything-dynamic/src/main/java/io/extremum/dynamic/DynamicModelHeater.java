package io.extremum.dynamic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.extremum.dynamic.events.DynamicModelRegisteredEvent;
import io.extremum.dynamic.schema.networknt.NetworkntSchema;
import io.extremum.dynamic.schema.provider.networknt.NetworkntSchemaProvider;
import io.extremum.dynamic.validator.exceptions.SchemaLoadingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicModelHeater implements ApplicationListener<ContextRefreshedEvent> {
    private final NetworkntSchemaProvider schemaProvider;
    private final GithubSchemaProperties githubSchemaProperties;

    private volatile boolean alreadyHeated = false;
    private final SchemaMetaService schemaMetaService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadyHeated) {
            log.info("Already heated");
        } else {
            alreadyHeated = true;

            String schemaName = githubSchemaProperties.getSchemaName();
            try {
                NetworkntSchema loaded = schemaProvider.loadSchema(schemaName);
                JsonNode title = loaded.getSchema().getSchemaNode().get("title");
                if (!isTextNode(title)) {
                    log.warn("No 'title' attribute found in schema {}. Model name for that schema can't be registered " +
                            "in a descriptor determinator", loaded.getSchema());
                } else {
                    schemaMetaService.registerMapping(title.textValue(), schemaName);
                    onMappingRegistered(title.textValue());
                }
            } catch (SchemaLoadingException e) {
                log.error("Unable to load schema {}", schemaName, e);
            }
        }
    }

    private void onMappingRegistered(String modelName) {
        eventPublisher.publishEvent(new DynamicModelRegisteredEvent(modelName));
    }

    private boolean isTextNode(JsonNode title) {
        return title instanceof TextNode;
    }
}
