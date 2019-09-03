package io.extremum.everything.aop;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.common.exceptions.ModelNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.DefaultAopProxyFactory;
import org.springframework.stereotype.Controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author rpuch
 */
class ConvertNullDescriptorToModelNotFoundAspectTest {
    private final ConvertNullDescriptorToModelNotFoundAspect aspect = new ConvertNullDescriptorToModelNotFoundAspect();
    private TestController controllerProxy;

    @BeforeEach
    void setUp() {
        AspectJProxyFactory aspectJProxyFactory = new AspectJProxyFactory(new TestController());
        aspectJProxyFactory.addAspect(aspect);

        DefaultAopProxyFactory proxyFactory = new DefaultAopProxyFactory();
        AopProxy aopProxy = proxyFactory.createAopProxy(aspectJProxyFactory);

        controllerProxy = (TestController) aopProxy.getProxy();
    }

    @Test
    void whenInvokingWithNullDescriptor_thenModelNotFoundExceptionShouldBeThrown() {
        try {
            controllerProxy.methodWithDescriptor(null);
            fail("An exception should be thrown");
        } catch (ModelNotFoundException e) {
            assertThat(e.getMessage(), is("No descriptor was found"));
        }
    }

    @Test
    void whenInvokingWithNonNullDescriptor_thenNothingShouldBeThrown() {
        String result = controllerProxy.methodWithDescriptor(Descriptor.builder().externalId("id").build());
        assertThat(result, is("ok"));
    }

    @Test
    void whenInvokingWithNullObjectWhichIsNotADescriptor_thenNothingShouldBeThrown() {
        String result = controllerProxy.methodWithoutDescriptor(null);
        assertThat(result, is("ok"));
    }

    @Controller
    @ConvertNullDescriptorToModelNotFound
    private static class TestController {
        @SuppressWarnings("unused")
        String methodWithDescriptor(Descriptor descriptor) {
            return "ok";
        }

        @SuppressWarnings({"SameParameterValue", "unused"})
        String methodWithoutDescriptor(Object object) {
            return "ok";
        }
    }
}