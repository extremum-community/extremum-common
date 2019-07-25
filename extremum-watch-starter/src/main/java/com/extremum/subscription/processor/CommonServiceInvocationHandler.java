package com.extremum.subscription.processor;

import com.extremum.common.models.Model;
import com.extremum.common.service.Problems;
import com.extremum.everything.support.ModelClasses;
import com.extremum.sharedmodels.annotation.UsesStaticDependencies;
import com.extremum.sharedmodels.descriptor.StaticDescriptorLoaderAccessor;
import com.extremum.subscription.annotation.CapturedModel;
import com.extremum.subscription.listener.WatchListener;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class CommonServiceInvocationHandler extends WatchInvocationHandler {
    private final ObjectMapper objectMapper;
    private final ModelClasses modelClasses;

    public CommonServiceInvocationHandler(List<WatchListener> watchListeners, Object proxiedBean, ModelClasses modelClasses, ObjectMapper objectMapper) {
        super(watchListeners, proxiedBean);
        this.modelClasses = modelClasses;
        this.objectMapper = objectMapper;
    }

    @Override
    @UsesStaticDependencies
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isSaveMethod(method, args)) {
            Model model = (Model) args[0];
            if (model.getClass().getAnnotation(CapturedModel.class) != null) {
                watchUpdate(String.format("Save model %s", objectMapper.writeValueAsString(model)));
            }
        } else if (isDeleteMethod(method, args)) {
            Class<Model> modelClass = StaticDescriptorLoaderAccessor.getDescriptorLoader()
                    .loadByInternalId((String) args[0])
                    .map(descriptor -> modelClasses.getClassByModelName(descriptor.getModelType()))
                    .orElse(null);
            if (modelClass != null && modelClass.getAnnotation(CapturedModel.class) != null) {
                watchUpdate(String.format("Delete model with id %s", args[0]));
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
