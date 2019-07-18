package com.extremum.everything.services.management;

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
class NullPatcherHooksTest {
    private final NullPatcherHooks<Model, RequestDto> hooks = new NullPatcherHooks<>();

    @Test
    void whenCallingAfterPatchApplied_thenTheDtoPassedAsAnArgumentShouldBeReturned() {
        RequestDto dto = mock(RequestDto.class);

        RequestDto result = hooks.afterPatchAppliedToDto(dto);

        assertThat(result, is(sameInstance(dto)));
    }
}