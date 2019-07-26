package com.extremum.watch.processor;

import com.extremum.common.models.Model;
import com.extremum.everything.support.ModelClasses;
import com.extremum.sharedmodels.annotation.CapturedModel;
import com.extremum.sharedmodels.annotation.UsesStaticDependencies;
import com.extremum.sharedmodels.descriptor.StaticDescriptorLoaderAccessor;
import com.extremum.watch.config.ExtremumKafkaProperties;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.repositories.TextWatchEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Aspect processor for {@link com.extremum.common.service.CommonService}
 */
@Service
public final class CommonServiceWatchProcessor extends WatchProcessor {
    private final ObjectMapper objectMapper;
    private final ModelClasses modelClasses;

    public CommonServiceWatchProcessor(ModelClasses modelClasses,
                                       ObjectMapper objectMapper, TextWatchEventRepository repository,
                                       KafkaTemplate<String, TextWatchEvent.TextWatchEventDto> kafkaTemplate, ExtremumKafkaProperties properties) {
        super(properties, repository, kafkaTemplate);
        this.modelClasses = modelClasses;
        this.objectMapper = objectMapper;
    }

    @UsesStaticDependencies
    public void process(JoinPoint jp) throws JsonProcessingException {
        Object[] args = jp.getArgs();
        if (isSaveMethod(jp)) {
            Model model = (Model) args[0];
            if (model.getClass().getAnnotation(CapturedModel.class) != null) {
                TextWatchEvent event = new TextWatchEvent("save", objectMapper.writeValueAsString(model));
                watchUpdate(event);
            }
        } else if (isDeleteMethod(jp)) {
            Class<Model> modelClass = StaticDescriptorLoaderAccessor.getDescriptorLoader()
                    .loadByInternalId((String) args[0])
                    .map(descriptor -> modelClasses.getClassByModelName(descriptor.getModelType()))
                    .orElse(null);
            if (modelClass != null && modelClass.getAnnotation(CapturedModel.class) != null) {
                TextWatchEvent event = new TextWatchEvent("delete", objectMapper.writeValueAsString(args[0]));
                watchUpdate(event);
            }
        }
    }

    private boolean isDeleteMethod(JoinPoint jp) {
        return jp.getSignature().getName().equals("delete");
    }

    private boolean isSaveMethod(JoinPoint jp) {
        return jp.getSignature().getName().equals("save");
    }
}
