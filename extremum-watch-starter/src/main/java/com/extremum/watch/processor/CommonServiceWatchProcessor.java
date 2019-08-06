package com.extremum.watch.processor;

import com.extremum.common.models.BasicModel;
import com.extremum.common.models.Model;
import com.extremum.everything.support.ModelClasses;
import com.extremum.sharedmodels.annotation.CapturedModel;
import com.extremum.sharedmodels.annotation.UsesStaticDependencies;
import com.extremum.sharedmodels.descriptor.StaticDescriptorLoaderAccessor;
import com.extremum.watch.models.TextWatchEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Processor for {@link com.extremum.common.service.CommonService} pointcut
 */
@Slf4j
@Service
public final class CommonServiceWatchProcessor {
    private final ObjectMapper objectMapper;
    private final ModelClasses modelClasses;
    private final WatchEventConsumer watchEventConsumer;

    public CommonServiceWatchProcessor(ModelClasses modelClasses,
            ObjectMapper objectMapper,
            WatchEventConsumer watchEventConsumer) {
        this.modelClasses = modelClasses;
        this.objectMapper = objectMapper;
        this.watchEventConsumer = watchEventConsumer;
    }

    @UsesStaticDependencies
    public void process(JoinPoint jp, Model returnedModel) throws JsonProcessingException {
        Object[] args = jp.getArgs();
        if (log.isDebugEnabled()) {
            log.debug("Captured method {} with args {}", jp.getSignature().getName(), Arrays.toString(args));
        }
        if (isSaveMethod(jp)) {
            Model model = (Model) args[0];
            if (model.getClass().getAnnotation(CapturedModel.class) != null
                    && BasicModel.class.isAssignableFrom(model.getClass())) {
                String jsonPatch = objectMapper.writeValueAsString(model);
                String modelInternalId = ((BasicModel) model).getId().toString();
                TextWatchEvent event = new TextWatchEvent(jsonPatch, modelInternalId, model);
                watchEventConsumer.consume(event);
            }
        } else if (isDeleteMethod(jp)) {
            String modelInternalId = (String) args[0];
            Class<Model> modelClass = StaticDescriptorLoaderAccessor.getDescriptorLoader()
                    .loadByInternalId(modelInternalId)
                    .map(descriptor -> modelClasses.getClassByModelName(descriptor.getModelType()))
                    .orElse(null);
            if (modelClass != null && modelClass.getAnnotation(CapturedModel.class) != null) {
                String jsonPatch = objectMapper.writeValueAsString(modelInternalId);
                TextWatchEvent event = new TextWatchEvent(jsonPatch, modelInternalId, returnedModel);
                // TODO: should we just ALWAYS set modification time in CommonService.delete()?
                event.touchModelMotificationTime();
                watchEventConsumer.consume(event);
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
