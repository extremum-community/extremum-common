package com.extremum.everything.services.management;

import com.extremum.common.models.MongoCommonModel;
import com.extremum.common.models.annotation.ModelName;
import com.extremum.everything.services.PatchPersistenceContext;
import com.extremum.everything.services.PatcherHooksService;
import com.extremum.sharedmodels.dto.RequestDto;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class PatcherHooksCollectionTest {
    private PatcherHooksCollection collection;

    @Spy
    private AModelHooksService aModelHooksService;
    @Spy
    private BModelHooksService bModelHooksService;

    @Mock
    private RequestDto requestDto;

    @BeforeEach
    void createPatcherHooksCollection() {
        collection = new PatcherHooksCollection(ImmutableList.of(aModelHooksService, bModelHooksService));
    }

    @Test
    void givenHooksExist_whenCallingAfterPatchAppliedToDto_thenOnlyApplicableHooksShouldBeCalled() {
        collection.afterPatchAppliedToDto("AModel", requestDto);

        verify(aModelHooksService).afterPatchAppliedToDto(requestDto);
        verify(bModelHooksService, never()).afterPatchAppliedToDto(requestDto);
    }

    @Test
    void givenNoHooksExist_whenCallingAfterPatchAppliedToDto_thenNoHooksShouldBeCalled() {
        collection.afterPatchAppliedToDto("WithoutHooks", requestDto);

        verify(aModelHooksService, never()).afterPatchAppliedToDto(requestDto);
        verify(bModelHooksService, never()).afterPatchAppliedToDto(requestDto);
    }

    @Test
    void givenHooksExist_whenCallingBeforeSave_thenOnlyApplicableHooksShouldBeCalled() {
        PatchPersistenceContext<AModel> context = new PatchPersistenceContext<>(new AModel(), new AModel());

        collection.beforeSave("AModel", context);

        verify(aModelHooksService).beforeSave(context);
        verify(bModelHooksService, never()).beforeSave(any());
    }

    @Test
    void givenNoHooksExist_whenCallingBeforeSave_thenNoHooksShouldBeCalled() {
        PatchPersistenceContext<WithoutHooks> context = new PatchPersistenceContext<>(
                new WithoutHooks(), new WithoutHooks());

        collection.beforeSave("WithoutHooks", context);

        verify(aModelHooksService, never()).beforeSave(any());
        verify(bModelHooksService, never()).afterPatchAppliedToDto(any());
    }

    @Test
    void givenHooksExist_whenCallingAfterSave_thenOnlyApplicableHooksShouldBeCalled() {
        PatchPersistenceContext<AModel> context = new PatchPersistenceContext<>(new AModel(), new AModel());

        collection.afterSave("AModel", context);

        verify(aModelHooksService).afterSave(context);
        verify(bModelHooksService, never()).afterSave(any());
    }

    @Test
    void givenNoHooksExist_whenCallingAfterSave_thenNoHooksShouldBeCalled() {
        PatchPersistenceContext<WithoutHooks> context = new PatchPersistenceContext<>(
                new WithoutHooks(), new WithoutHooks());

        collection.afterSave("WithoutHooks", context);

        verify(aModelHooksService, never()).afterSave(any());
        verify(bModelHooksService, never()).afterPatchAppliedToDto(any());
    }

    @ModelName("AModel")
    private static class AModel extends MongoCommonModel {
    }

    @ModelName("BModel")
    private static class BModel extends MongoCommonModel {
    }

    @ModelName("WithoutHooks")
    private static class WithoutHooks extends MongoCommonModel {
    }

    private static class AModelHooksService implements PatcherHooksService<AModel, RequestDto> {
        @Override
        public String getSupportedModel() {
            return "AModel";
        }
    }

    private static class BModelHooksService implements PatcherHooksService<BModel, RequestDto> {
        @Override
        public String getSupportedModel() {
            return "BModel";
        }
    }
}