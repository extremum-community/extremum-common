package io.extremum.watch.aop;

import io.extremum.common.dao.MongoCommonDao;
import io.extremum.common.models.MongoCommonModel;
import io.extremum.common.service.CommonService;
import io.extremum.common.service.ThrowOnAlert;
import io.extremum.common.service.impl.MongoCommonServiceImpl;
import io.extremum.everything.services.management.PatchFlow;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.watch.processor.CommonServiceWatchProcessor;
import io.extremum.watch.processor.Invocation;
import io.extremum.watch.processor.PatchFlowWatchProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.google.common.util.concurrent.MoreExecutors;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.DefaultAopProxyFactory;

import java.util.Collections;
import java.util.concurrent.Executor;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
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
    @Spy
    private Executor executor = MoreExecutors.sameThreadExecutor();

    private CommonService<TestModel> commonServiceProxy;
    private PatchFlow patchFlowProxy;

    @Mock
    private MongoCommonDao<TestModel> dao;
    @Mock
    private PatchFlow originalPatchFlow;
    @Captor
    private ArgumentCaptor<Invocation> invocationCaptor;

    @BeforeEach
    void createProxies() {
        commonServiceProxy = wrapWithAspect(new TestCommonService(dao));
        patchFlowProxy = wrapWithAspect(originalPatchFlow);
    }

    @AfterEach
    void clearPatchingFlag() {
        WatchCaptureContext.exitPatching();
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

        assertThatSaveInvocationIsInvokedWith(model);
    }

    private void assertThatSaveInvocationIsInvokedWith(TestModel model) throws JsonProcessingException {
        verify(commonServiceWatchProcessor).process(invocationCaptor.capture(), same(model));
        Invocation invocation = invocationCaptor.getValue();
        assertThat(invocation.methodName(), is("save"));
        assertThat(invocation.args().length, is(greaterThanOrEqualTo(1)));
        assertThat(invocation.args()[0], is(sameInstance(model)));
    }

    @Test
    void whenInvokingSaveWithProblemsMethodOnCommonService_thenCommonServiceWatchProcessorShouldBeTriggered()
            throws Exception {
        TestModel model = new TestModel();
        when(dao.save(model)).thenReturn(model);

        commonServiceProxy.save(model, new ThrowOnAlert());

        assertThatSaveInvocationIsInvokedWith(model);
    }

    @Test
    void whenInvokingDeleteMethodOnCommonService_thenCommonServiceWatchProcessorShouldBeTriggered() throws Exception {
        String internalId = new ObjectId().toString();
        TestModel model = new TestModel();
        when(dao.deleteByIdAndReturn(any())).thenReturn(model);

        commonServiceProxy.delete(internalId);

        assertThatDeleteInvocationIsInvokedWith(internalId, model);
    }

    private void assertThatDeleteInvocationIsInvokedWith(String internalId,
            TestModel model) throws JsonProcessingException {
        verify(commonServiceWatchProcessor).process(invocationCaptor.capture(), same(model));
        Invocation invocation = invocationCaptor.getValue();
        assertThat(invocation.methodName(), is("delete"));
        assertThat(invocation.args().length, is(greaterThanOrEqualTo(1)));
        assertThat(invocation.args()[0], is(equalTo(internalId)));
    }

    @Test
    void whenInvokingDeleteWithProblemsMethodOnCommonService_thenCommonServiceWatchProcessorShouldBeTriggered()
            throws Exception {
        String internalId = new ObjectId().toString();
        TestModel model = new TestModel();
        when(dao.deleteByIdAndReturn(any())).thenReturn(model);

        commonServiceProxy.delete(internalId, new ThrowOnAlert());

        assertThatDeleteInvocationIsInvokedWith(internalId, model);
    }

    @Test
    void whenInvokingPatchMethodOnPatchFlow_thenPatchFlowWatchProcessorShouldBeTriggered() throws Exception {
        Descriptor descriptor = new Descriptor("external-id");
        JsonPatch jsonPatch = aJsonPatch();

        TestModel model = new TestModel();
        when(originalPatchFlow.patch(any(), any())).thenReturn(model);

        patchFlowProxy.patch(descriptor, jsonPatch);

        verify(patchFlowWatchProcessor).process(invocationCaptor.capture(), same(model));
        Invocation invocation = invocationCaptor.getValue();
        assertThat(invocation.methodName(), is("patch"));
        assertThat(invocation.args().length, is(2));
        assertThat(invocation.args()[0], is(sameInstance(descriptor)));
        assertThat(invocation.args()[1], is(sameInstance(jsonPatch)));
    }

    @NotNull
    private JsonPatch aJsonPatch() {
        return new JsonPatch(Collections.emptyList());
    }

    @Test
    void beforePatchingThePatchingFlagShouldNotBeSet() {
        assertThat(WatchCaptureContext.isPatching(), is(false));
    }

    @Test
    void whenPatchIsIntercepted_thenInsidePatchProcessingPatchingFlagIsSet() {
        when(originalPatchFlow.patch(any(), any())).then(invocation -> {
            assertThat(WatchCaptureContext.isPatching(), is(true));
            return null;
        });

        patchFlowProxy.patch(new Descriptor("external-id"), aJsonPatch());
    }

    @Test
    void whenPatchIsIntercepted_afterPatchFlowFinishesThePatchingFlagShouldNotBeSet() {
        when(originalPatchFlow.patch(any(), any())).thenReturn(new TestModel());

        patchFlowProxy.patch(new Descriptor("external-id"), aJsonPatch());

        assertThat(WatchCaptureContext.isPatching(), is(false));
    }

    @Test
    void whenInvokingSaveMethodOnCommonServiceAndPatchingFlagIsSet_thenTheInvocationShouldBeIgnored() throws Exception {
        WatchCaptureContext.enterPatching();

        commonServiceProxy.save(new TestModel());

        verify(commonServiceWatchProcessor, never()).process(any(), any());
    }

    private static class TestModel extends MongoCommonModel {
    }

    private static class TestCommonService extends MongoCommonServiceImpl<TestModel> {
        TestCommonService(MongoCommonDao<TestModel> dao) {
            super(dao);
        }
    }
}