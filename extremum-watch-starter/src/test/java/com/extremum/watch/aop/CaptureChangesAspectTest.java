package com.extremum.watch.aop;

import com.extremum.common.dao.MongoCommonDao;
import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.service.CommonService;
import com.extremum.common.service.impl.MongoCommonServiceImpl;
import com.extremum.everything.services.management.PatchFlow;
import com.extremum.sharedmodels.descriptor.Descriptor;
import com.extremum.watch.processor.CommonServiceWatchProcessor;
import com.extremum.watch.processor.PatchFlowWatchProcessor;
import com.github.fge.jsonpatch.JsonPatch;
import org.aspectj.lang.JoinPoint;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.DefaultAopProxyFactory;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class CaptureChangesAspectTest {
    @InjectMocks
    private CaptureChangesAspect aspect;

    @Mock
    private CommonServiceWatchProcessor commonServiceWatchProcessor;
    @Mock
    private PatchFlowWatchProcessor patchFlowWatchProcessor;

    private CommonService<TestModel> commonServiceProxy;
    private PatchFlow patchFlowProxy;

    @Mock
    private MongoCommonDao<TestModel> dao;
    @Mock
    private PatchFlow originalPatchFlow;
    @Captor
    private ArgumentCaptor<JoinPoint> joinPointCaptor;

    @BeforeEach
    void setUp() {
        commonServiceProxy = wrapWithAspect(new TestCommonService(dao));
        patchFlowProxy = wrapWithAspect(originalPatchFlow);
    }

    private <T> T wrapWithAspect(T proxiedObject) {
        AspectJProxyFactory aspectJProxyFactory = new AspectJProxyFactory(proxiedObject);
        aspectJProxyFactory.addAspect(aspect);

        DefaultAopProxyFactory proxyFactory = new DefaultAopProxyFactory();
        AopProxy aopProxy = proxyFactory.createAopProxy(aspectJProxyFactory);

        @SuppressWarnings("unchecked")
        T castProxy = (T) aopProxy.getProxy();
        return castProxy;
    }

    @Test
    void whenInvokingSaveMethodOnCommonService_thenCommonServiceWatchProcessorShouldBeTriggered() throws Exception {
        TestModel model = new TestModel();
        when(dao.save(model)).thenReturn(model);

        commonServiceProxy.save(model);

        verify(commonServiceWatchProcessor).process(joinPointCaptor.capture(), same(model));
        JoinPoint joinPoint = joinPointCaptor.getValue();
        assertThat(joinPoint.getSignature().getName(), is("save"));
        assertThat(joinPoint.getArgs().length, is(1));
        assertThat(joinPoint.getArgs()[0], is(sameInstance(model)));
    }

    @Test
    void whenInvokingDeleteMethodOnCommonService_thenCommonServiceWatchProcessorShouldBeTriggered() throws Exception {
        String internalId = new ObjectId().toString();
        TestModel model = new TestModel();
        when(dao.deleteByIdAndReturn(any())).thenReturn(model);

        commonServiceProxy.delete(internalId);

        verify(commonServiceWatchProcessor).process(joinPointCaptor.capture(), same(model));
        JoinPoint joinPoint = joinPointCaptor.getValue();
        assertThat(joinPoint.getSignature().getName(), is("delete"));
        assertThat(joinPoint.getArgs().length, is(1));
        assertThat(joinPoint.getArgs()[0], is(equalTo(internalId)));
    }

    @Test
    void whenInvokingPatchMethodOnPatchFlow_thenPatchFlowWatchProcessorShouldBeTriggered() throws Exception {
        Descriptor descriptor = new Descriptor("external-id");
        JsonPatch jsonPatch = new JsonPatch(Collections.emptyList());

        TestModel model = new TestModel();
        when(originalPatchFlow.patch(any(), any())).thenReturn(model);

        patchFlowProxy.patch(descriptor, jsonPatch);

        verify(patchFlowWatchProcessor).process(joinPointCaptor.capture(), same(model));
        JoinPoint joinPoint = joinPointCaptor.getValue();
        assertThat(joinPoint.getSignature().getName(), is("patch"));
        assertThat(joinPoint.getArgs().length, is(2));
        assertThat(joinPoint.getArgs()[0], is(sameInstance(descriptor)));
        assertThat(joinPoint.getArgs()[1], is(sameInstance(jsonPatch)));
    }

    private static class TestModel extends MongoCommonModel {
    }

    private static class TestCommonService extends MongoCommonServiceImpl<TestModel> {
        public TestCommonService(MongoCommonDao<TestModel> dao) {
            super(dao);
        }
    }
}