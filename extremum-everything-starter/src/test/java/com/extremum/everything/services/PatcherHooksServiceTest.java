package com.extremum.everything.services;

import com.extremum.common.models.Model;
import com.extremum.sharedmodels.dto.RequestDto;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author rpuch
 */
class PatcherHooksServiceTest {
    @Test
    void whenCallingDefaultAfterPatchAppliedDto_thenTheArgumentShouldBeReturned() {
        RequestDto dto = mock(RequestDto.class);
        PatcherHooksService<Model, RequestDto> hooksService = new DefaultHooksService();

        RequestDto newDto = hooksService.afterPatchAppliedToDto(dto);

        assertThat(newDto, is(sameInstance(dto)));
    }

    private static class DefaultHooksService implements PatcherHooksService<Model, RequestDto> {
        @Override
        public String getSupportedModel() {
            return "Model";
        }
    }
}