package com.extremum.watch.aop;

import com.extremum.common.models.Model;
import com.extremum.watch.processor.CommonServiceWatchProcessor;
import com.extremum.watch.processor.MethodJoinPointInvocation;
import com.extremum.watch.processor.PatchFlowWatchProcessor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.Executor;

/**
 * Aspect to implement watch logic.
 * Have different pointcuts and different handlers to capture events.
 */
@Component
@Slf4j
@Aspect
public class CaptureChangesAspect {
    private final PatchFlowWatchProcessor patchFlowProcessor;
    private final CommonServiceWatchProcessor commonServiceProcessor;
    private final Executor executor;

    public CaptureChangesAspect(PatchFlowWatchProcessor patchFlowProcessor,
            CommonServiceWatchProcessor commonServiceProcessor,
            @Qualifier("watchEventsHandlingExecutor") Executor executor) {
        this.patchFlowProcessor = patchFlowProcessor;
        this.commonServiceProcessor = commonServiceProcessor;
        this.executor = executor;
    }

    @AfterReturning(value = "patchMethod()", returning = "returnedModel")
    public void watchPatchChanges(JoinPoint jp, Model returnedModel) {
        executor.execute(() -> processPatchChanges(jp, returnedModel));
    }

    private void processPatchChanges(JoinPoint jp, Model returnedModel) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Watch PatchFlow method with name {} and args {}",
                        jp.getSignature().getName(), Arrays.toString(jp.getArgs()));
            }
            patchFlowProcessor.process(new MethodJoinPointInvocation(jp), returnedModel);
        } catch (Exception e) {
            log.error("Exception on watchPatchChanges() : ", e);
        }
    }

    @AfterReturning(value = "commonServiceDeleteMethods() || commonServiceSaveMethods()", returning = "returnedModel")
    public void watchCommonServiceChanges(JoinPoint jp, Model returnedModel) {
        executor.execute(() -> processCommonServiceInvocation(jp, returnedModel));
    }

    private void processCommonServiceInvocation(JoinPoint jp, Model returnedModel) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Watch CommonService method with name {} and args {}",
                        jp.getSignature().getName(), Arrays.toString(jp.getArgs()));
            }
            commonServiceProcessor.process(new MethodJoinPointInvocation(jp), returnedModel);
        } catch (Exception e) {
            log.error("Exception on watchCommonServiceChanges() : ", e);
        }
    }

    @Pointcut("execution(* com.extremum.everything.services.management.PatchFlow+.patch(..))")
    public void patchMethod() {
    }

    @Pointcut("execution(* com.extremum.common.service.CommonService+.delete(..))")
    public void commonServiceDeleteMethods() {
    }

    @Pointcut("execution(* com.extremum.common.service.CommonService+.save(..))")
    public void commonServiceSaveMethods() {
    }

    // TODO: add ElasticsearchCommonService.patch(...) methods here?
}
