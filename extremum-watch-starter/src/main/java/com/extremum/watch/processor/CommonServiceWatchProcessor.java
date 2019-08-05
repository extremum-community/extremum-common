package com.extremum.watch.processor;

import com.extremum.common.models.BasicModel;
import com.extremum.common.models.Model;
import com.extremum.everything.support.ModelClasses;
import com.extremum.sharedmodels.annotation.CapturedModel;
import com.extremum.sharedmodels.annotation.UsesStaticDependencies;
import com.extremum.sharedmodels.descriptor.StaticDescriptorLoaderAccessor;
import com.extremum.watch.config.ExtremumKafkaProperties;
import com.extremum.watch.dto.TextWatchEventNotificationDto;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.repositories.TextWatchEventRepository;
import com.extremum.watch.services.WatchSubscriptionService;
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
            WatchSubscriptionService watchSubscriptionService,
            KafkaTemplate<String, TextWatchEventNotificationDto> kafkaTemplate,
            ExtremumKafkaProperties properties) {
        super(properties, kafkaTemplate, repository, watchSubscriptionService);
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
                String jsonPatch = objectMapper.writeValueAsString(model);
                String modelInternalId = ((BasicModel) model).getId().toString();
                TextWatchEvent event = new TextWatchEvent("save", jsonPatch, modelInternalId);
                watchUpdate(event);
            }
        } else if (isDeleteMethod(jp)) {
            String modelInternalId = (String) args[0];
            Class<Model> modelClass = StaticDescriptorLoaderAccessor.getDescriptorLoader()
                    .loadByInternalId(modelInternalId)
                    .map(descriptor -> modelClasses.getClassByModelName(descriptor.getModelType()))
                    .orElse(null);
            if (modelClass != null && modelClass.getAnnotation(CapturedModel.class) != null) {
                String jsonPatch = objectMapper.writeValueAsString(modelInternalId);
                TextWatchEvent event = new TextWatchEvent("delete", jsonPatch, modelInternalId);
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
