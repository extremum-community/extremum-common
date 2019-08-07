package com.extremum.watch.processor;

import com.extremum.common.descriptor.service.DescriptorService;
import com.extremum.common.models.BasicModel;
import com.extremum.common.models.Model;
import com.extremum.everything.support.ModelClasses;
import com.extremum.sharedmodels.annotation.CapturedModel;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.watch.models.TextWatchEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Processor for {@link com.extremum.common.service.CommonService} pointcut
 */
@Slf4j
@Service
public class CommonServiceWatchProcessor {
    private final ObjectMapper objectMapper;
    private final DescriptorService descriptorService;
    private final ModelClasses modelClasses;
    private final WatchEventConsumer watchEventConsumer;

    public CommonServiceWatchProcessor(ModelClasses modelClasses,
            ObjectMapper objectMapper,
            DescriptorService descriptorService,
            WatchEventConsumer watchEventConsumer) {
        this.modelClasses = modelClasses;
        this.objectMapper = objectMapper;
        this.descriptorService = descriptorService;
        this.watchEventConsumer = watchEventConsumer;
    }

    public void process(Invocation invocation, Model returnedModel) throws JsonProcessingException {
        Object[] args = invocation.args();
        if (log.isDebugEnabled()) {
            log.debug("Captured method {} with args {}", invocation.methodName(), Arrays.toString(args));
        }
        if (isSaveMethod(invocation)) {
            Model model = (Model) args[0];
            if (model.getClass().getAnnotation(CapturedModel.class) != null
                    && BasicModel.class.isAssignableFrom(model.getClass())) {
                String jsonPatch = objectMapper.writeValueAsString(model);
                String modelInternalId = ((BasicModel) model).getId().toString();
                TextWatchEvent event = new TextWatchEvent(jsonPatch, modelInternalId, model);
                watchEventConsumer.consume(event);
            }
        } else if (isDeleteMethod(invocation)) {
            String modelInternalId = (String) args[0];
            Class<Model> modelClass = descriptorService.loadByInternalId(modelInternalId)
                    .map(Descriptor::getModelType)
                    .map(modelClasses::getClassByModelName)
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

    private boolean isDeleteMethod(Invocation invocation) {
        return "delete".equals(invocation.methodName());
    }

    private boolean isSaveMethod(Invocation invocation) {
        return "save".equals(invocation.methodName());
    }
}
