package io.extremum.watch.processor;

import io.extremum.common.descriptor.service.DescriptorService;
import io.extremum.common.dto.converters.services.DtoConversionService;
import io.extremum.common.model.BasicModel;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.common.service.CommonService;
import io.extremum.common.support.ModelClasses;
import io.extremum.watch.config.conditional.BlockingWatchConfiguration;
import io.extremum.watch.models.TextWatchEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import static io.extremum.watch.processor.JsonPatchUtils.constructFullReplaceJsonPatch;

/**
 * Processor for {@link CommonService} pointcut
 */

@Slf4j
@Service
@ConditionalOnBean(BlockingWatchConfiguration.class)
public class CommonServiceWatchProcessor extends CommonServiceWatchProcessorBase {
    public CommonServiceWatchProcessor(ObjectMapper objectMapper,
                                       DescriptorService descriptorService,
                                       ModelClasses modelClasses,
                                       DtoConversionService dtoConversionService,
                                       WatchEventConsumer watchEventConsumer) {
        super(objectMapper, descriptorService, modelClasses, dtoConversionService, watchEventConsumer);
    }

    protected void processSave(Object[] args) throws JsonProcessingException {
        Model model = (Model) args[0];
        if (isModelWatched(model) && model instanceof BasicModel) {
            String jsonPatchString = constructFullReplaceJsonPatch(objectMapper, dtoConversionService, model);
            String modelInternalId = ((BasicModel) model).getId().toString();
            TextWatchEvent event = new TextWatchEvent(jsonPatchString, null, modelInternalId, model);
            watchEventConsumer.consume(event);
        }
    }
}
