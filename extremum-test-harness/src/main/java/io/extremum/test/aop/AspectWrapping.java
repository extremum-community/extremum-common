package io.extremum.test.aop;

import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.DefaultAopProxyFactory;

public class AspectWrapping {
    public static <T> T wrapInAspect(T objectToWrap, Object aspect) {
        AspectJProxyFactory aspectJProxyFactory = new AspectJProxyFactory(objectToWrap);
        aspectJProxyFactory.addAspect(aspect);

        DefaultAopProxyFactory proxyFactory = new DefaultAopProxyFactory();
        AopProxy aopProxy = proxyFactory.createAopProxy(aspectJProxyFactory);

        @SuppressWarnings("unchecked") T castResult = (T) aopProxy.getProxy();
        return castResult;
    }
}
