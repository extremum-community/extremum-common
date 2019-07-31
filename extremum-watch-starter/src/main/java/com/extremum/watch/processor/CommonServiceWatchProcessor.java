package com.extremum.watch.processor;

import com.extremum.common.models.BasicModel;
import com.extremum.common.models.Model;
import com.extremum.everything.support.ModelClasses;
import com.extremum.sharedmodels.annotation.CapturedModel;
import com.extremum.sharedmodels.annotation.UsesStaticDependencies;
import com.extremum.sharedmodels.descriptor.StaticDescriptorLoaderAccessor;
import com.extremum.watch.config.ExtremumKafkaProperties;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.repositories.TextWatchEventRepository;
import com.extremum.watch.services.SubscriptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Processor for {@link com.extremum.common.service.CommonService} pointcut
 */
@Slf4j
@Service
public final class CommonServiceWatchProcessor extends WatchProcessor {
    private final ObjectMapper objectMapper;
    private final ModelClasses modelClasses;

    public CommonServiceWatchProcessor(ModelClasses modelClasses,
                                       ObjectMapper objectMapper, TextWatchEventRepository repository,
                                       SubscriptionService subscriptionService,
                                       KafkaTemplate<String, TextWatchEvent.TextWatchEventDto> kafkaTemplate, ExtremumKafkaProperties properties) {
        super(properties, kafkaTemplate, repository, subscriptionService);
        this.modelClasses = modelClasses;
        this.objectMapper = objectMapper;
    }

    @UsesStaticDependencies
    public void process(JoinPoint jp) throws JsonProcessingException {
        Object[] args = jp.getArgs();
        log.debug("Captured method {} with args {}", jp.getSignature().getName(), Arrays.toString(args));
        if (isSaveMethod(jp)) {
            Model model = (Model) args[0];
            if (model.getClass().getAnnotation(CapturedModel.class) != null
                    && BasicModel.class.isAssignableFrom(model.getClass())) {
                TextWatchEvent event = new TextWatchEvent("save", objectMapper.writeValueAsString(model), ((BasicModel) model).getId().toString());
                watchUpdate(event);
            }
        } else if (isDeleteMethod(jp)) {
            String modelId = (String) args[0];
            Class<Model> modelClass = StaticDescriptorLoaderAccessor.getDescriptorLoader()
                    .loadByInternalId(modelId)
                    .map(descriptor -> modelClasses.getClassByModelName(descriptor.getModelType()))
                    .orElse(null);
            if (modelClass != null && modelClass.getAnnotation(CapturedModel.class) != null) {
                TextWatchEvent event = new TextWatchEvent("delete", objectMapper.writeValueAsString(modelId), modelId);
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