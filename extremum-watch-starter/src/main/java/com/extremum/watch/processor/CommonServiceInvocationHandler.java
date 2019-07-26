package com.extremum.watch.processor;

import com.extremum.common.models.Model;
import com.extremum.common.service.Problems;
import com.extremum.everything.support.ModelClasses;
import com.extremum.sharedmodels.annotation.UsesStaticDependencies;
import com.extremum.sharedmodels.descriptor.StaticDescriptorLoaderAccessor;
import com.extremum.watch.config.ExtremumKafkaProperties;
import com.extremum.watch.models.TextWatchEvent;
import com.extremum.watch.repositories.TextWatchEventRepository;
import com.extremum.watch.subscription.annotation.CapturedModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;

import java.lang.reflect.Method;
import java.util.Arrays;

public class CommonServiceInvocationHandler extends WatchInvocationHandler {
    private final ObjectMapper objectMapper;
    private final ModelClasses modelClasses;

    public CommonServiceInvocationHandler(Object proxiedBean, ModelClasses modelClasses,
                                          ObjectMapper objectMapper, TextWatchEventRepository repository,
                                          KafkaTemplate<String, TextWatchEvent.TextWatchEventDto> kafkaTemplate, ExtremumKafkaProperties properties) {
        super(proxiedBean, repository, kafkaTemplate, properties);
        this.modelClasses = modelClasses;
        this.objectMapper = objectMapper;
    }

    @Override
    @UsesStaticDependencies
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isSaveMethod(method, args)) {
            Model model = (Model) args[0];
            if (model.getClass().getAnnotation(CapturedModel.class) != null) {
                TextWatchEvent event = new TextWatchEvent("save", objectMapper.writeValueAsString(model));
                watchUpdate(event);
            }
        } else if (isDeleteMethod(method, args)) {
            Class<Model> modelClass = StaticDescriptorLoaderAccessor.getDescriptorLoader()
                    .loadByInternalId((String) args[0])
                    .map(descriptor -> modelClasses.getClassByModelName(descriptor.getModelType()))
                    .orElse(null);
            if (modelClass != null && modelClass.getAnnotation(CapturedModel.class) != null) {
                TextWatchEvent event = new TextWatchEvent("delete", objectMapper.writeValueAsString(args[0]));
                watchUpdate(event);
            }
        }
        return method.invoke(super.getOriginalBean(), args);
    }

    private boolean isDeleteMethod(Method method, Object[] args) {
        return method.getName().equals("delete")
                && args.length == 2
                && Arrays.equals(method.getParameterTypes(), new Class[]{String.class, Problems.class});
    }

    private boolean isSaveMethod(Method method, Object[] args) {
        return method.getName().equals("save")
                && args.length == 2
                && method.getParameterTypes()[0].isAssignableFrom(Model.class);
    }
}
