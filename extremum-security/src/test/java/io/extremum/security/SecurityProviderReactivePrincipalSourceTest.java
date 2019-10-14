package io.extremum.security;

import io.extremum.authentication.api.ReactiveSecurityProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class SecurityProviderReactivePrincipalSourceTest {
    @InjectMocks
    private SecurityProviderReactivePrincipalSource principalSource;

    @Mock
    private ReactiveSecurityProvider securityProvider;

    @Test
    void givenPrincipalInSecurityProviderIsAlex_whenGettingPrincipal_thenAlexShouldBeReturned() {
        when(securityProvider.getPrincipal()).thenReturn(Mono.just("Alex"));

        assertThat(principalSource.getPrincipal().block(), is("Alex"));
    }

    @Test
    void givenPrincipalInSecurityProviderIs42_whenGettingPrincipal_then42StringShouldBeReturned() {
        when(securityProvider.getPrincipal()).thenReturn(Mono.just(42));

        assertThat(principalSource.getPrincipal().block(), is("42"));
    }

    @Test
    void givenPrincipalInSecurityProviderIsNull_whenGettingPrincipal_thenNullShouldBeReturned() {
        when(securityProvider.getPrincipal()).thenReturn(Mono.empty());

        assertThat(principalSource.getPrincipal().block(), is(nullValue()));
    }
}