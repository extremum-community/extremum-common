package com.extremum.everything.security;

/**
 * @author rpuch
 */
public class EverythingSecuredParser {
    public EverythingSecuredConfig parse(EverythingSecured everythingSecured) {
        return new EverythingSecuredConfig(
                everythingSecured.defaultAccess(),
                everythingSecured.get(),
                everythingSecured.patch(),
                everythingSecured.remove()
        );
    }
}
