package com.extremum.everything.security;

import io.extremum.authentication.api.SecurityProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class SecurityProviderPrincipalSourceTest {
    @InjectMocks
    private SecurityProviderPrincipalSource principalSource;

    @Mock
    private SecurityProvider securityProvider;

    @Test
    void givenPrincipalInSecurityProviderIsAlex_whenGettingPrincipal_thenAlexShouldBeReturned() {
        when(securityProvider.getPrincipal()).thenReturn("Alex");

        assertThat(principalSource.getPrincipal(), is("Alex"));
    }

    @Test
    void givenPrincipalInSecurityProviderIs42_whenGettingPrincipal_then42StringShouldBeReturned() {
        when(securityProvider.getPrincipal()).thenReturn(42);

        assertThat(principalSource.getPrincipal(), is("42"));
    }

    @Test
    void givenPrincipalInSecurityProviderIsNull_whenGettingPrincipal_thenNullShouldBeReturned() {
        when(securityProvider.getPrincipal()).thenReturn(null);

        assertThat(principalSource.getPrincipal(), is(nullValue()));
    }
}