package com.extremum.everything.security;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class EverythingSecuredParserTest {
    private EverythingSecuredParser parser = new EverythingSecuredParser();

    @Test
    void givenIndividualAccessIsSpecified_whenGettingAccess_thenIndividualAccessShouldBeReturned() {
        EverythingSecured everythingSecured = Individual.class.getAnnotation(EverythingSecured.class);
        EverythingSecuredConfig config = parser.parse(everythingSecured);

        assertThat(config.rolesForGet(), equalTo(new String[]{"GET"}));
        assertThat(config.rolesForPatch(), equalTo(new String[]{"PATCH"}));
        assertThat(config.rolesForRemove(), equalTo(new String[]{"REMOVE"}));
    }

    @Test
    void givenDefaultAccessIsSpecified_whenGettingAccess_thenDefaultAccessShouldBeReturned() {
        EverythingSecured everythingSecured = WithDefault.class.getAnnotation(EverythingSecured.class);
        EverythingSecuredConfig config = parser.parse(everythingSecured);

        assertThat(config.rolesForGet(), equalTo(new String[]{"DEFAULT"}));
        assertThat(config.rolesForPatch(), equalTo(new String[]{"DEFAULT"}));
        assertThat(config.rolesForRemove(), equalTo(new String[]{"DEFAULT"}));
    }

    @EverythingSecured(
            defaultAccess = @Access("DEFAULT"),
            get = @Access("GET"),
            patch = @Access("PATCH"),
            remove = @Access("REMOVE")
    )
    private static class Individual {
    }

    @EverythingSecured(
            defaultAccess = @Access("DEFAULT")
    )
    private static class WithDefault {
    }
}