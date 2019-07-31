package com.extremum.watch.aop;

import com.extremum.watch.processor.CommonServiceWatchProcessor;
import com.extremum.watch.processor.PatchFlowWatchProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect to implement watch logic.
 * Have different pointcuts and different handlers to capture events.
 */
@Component
@Slf4j
@Aspect
@RequiredArgsConstructor
public class CaptureChangesAspect {
    private final PatchFlowWatchProcessor patchFlowProcessor;
    private final CommonServiceWatchProcessor commonServiceProcessor;

    @AfterReturning("patchMethod()")
    public void watchPatchChanges(JoinPoint jp) {
        try {
            log.debug("Watch PatchFlow method with name {} and args {}", jp.getSignature().getName(), Arrays.toString(jp.getArgs()));
            patchFlowProcessor.process(jp);
        } catch (Exception e) {
            log.error("Exception on watchPatchChanges() : ", e);
        }
    }

    @AfterReturning("commonServiceDeleteMethods() || commonServiceSaveMethods()")
    public void watchCommonServiceChanges(JoinPoint jp) {
        try {
            log.debug("Watch CommonService method with name {} and args {}", jp.getSignature().getName(), Arrays.toString(jp.getArgs()));
            commonServiceProcessor.process(jp);
        } catch (JsonProcessingException e) {
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
}
