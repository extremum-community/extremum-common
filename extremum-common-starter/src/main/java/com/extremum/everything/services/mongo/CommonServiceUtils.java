package com.extremum.everything.services.mongo;

import com.extremum.common.models.Model;
import com.extremum.common.service.CommonService;
import org.springframework.core.ResolvableType;

/**
 * @author rpuch
 */
class CommonServiceUtils {
    static boolean isCommonServiceOfModelClass(CommonService<?, ?> service, Class<? extends Model> modelClass) {
        ResolvableType commonServiceInterface = findCommonServiceInterface(service);

        for (ResolvableType generic : commonServiceInterface.getGenerics()) {
            Class<?> resolvedGeneric = generic.resolve(Object.class);
            if (Model.class.isAssignableFrom(resolvedGeneric)) {
                return modelClass == resolvedGeneric;
            }
        }

        return false;
    }

    private static ResolvableType findCommonServiceInterface(CommonService<?, ?> service) {
        ResolvableType currentType = ResolvableType.forClass(service.getClass());
        ResolvableType commonServiceInterface = ResolvableType.NONE;

        do {
            for (ResolvableType iface : currentType.getInterfaces()) {
                if (iface.getRawClass() == CommonService.class) {
                    commonServiceInterface = iface;
                    break;
                }
            }
            currentType = currentType.getSuperType();
        } while (commonServiceInterface == ResolvableType.NONE && currentType != ResolvableType.NONE);

        return commonServiceInterface;
    }
}
