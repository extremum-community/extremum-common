package com.extremum.everything.security;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class EverythingRequiredRolesParserTest {
    private EverythingRequiredRolesParser parser = new EverythingRequiredRolesParser();

    @Test
    void givenIndividualAccessIsSpecified_whenGettingAccess_thenIndividualAccessShouldBeReturned() {
        EverythingRequiredRoles everythingRequiredRoles = Individual.class.getAnnotation(EverythingRequiredRoles.class);
        EverythingRequiredRolesConfig config = parser.parse(everythingRequiredRoles);

        assertThat(config.rolesForGet(), equalTo(new String[]{"GET"}));
        assertThat(config.rolesForPatch(), equalTo(new String[]{"PATCH"}));
        assertThat(config.rolesForRemove(), equalTo(new String[]{"REMOVE"}));
    }

    @Test
    void givenDefaultAccessIsSpecified_whenGettingAccess_thenDefaultAccessShouldBeReturned() {
        EverythingRequiredRoles everythingRequiredRoles = WithDefault.class.getAnnotation(EverythingRequiredRoles.class);
        EverythingRequiredRolesConfig config = parser.parse(everythingRequiredRoles);

        assertThat(config.rolesForGet(), equalTo(new String[]{"DEFAULT"}));
        assertThat(config.rolesForPatch(), equalTo(new String[]{"DEFAULT"}));
        assertThat(config.rolesForRemove(), equalTo(new String[]{"DEFAULT"}));
    }

    @EverythingRequiredRoles(
            defaultAccess = @Access("DEFAULT"),
            get = @Access("GET"),
            patch = @Access("PATCH"),
            remove = @Access("REMOVE")
    )
    private static class Individual {
    }

    @EverythingRequiredRoles(
            defaultAccess = @Access("DEFAULT")
    )
    private static class WithDefault {
    }
}