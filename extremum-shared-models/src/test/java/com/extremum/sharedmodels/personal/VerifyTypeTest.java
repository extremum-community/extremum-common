package com.extremum.sharedmodels.personal;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author rpuch
 */
class VerifyTypeTest {
    @Test
    void emptyListIsCompatible() {
        assertTrue(VerifyType.mutuallyCompatible(emptyList()));
    }

    @Test
    void singletonIsCompatible() {
        assertTrue(VerifyType.mutuallyCompatible(singletonList(VerifyType.EMAIL)));
    }

    @Test
    void duplicatesAreNotCompatible() {
        assertFalse(VerifyType.mutuallyCompatible(asList(VerifyType.USERNAME, VerifyType.USERNAME)));
    }

    @Test
    void usernameAndPasswordShouldBeCompatible() {
        assertTrue(VerifyType.mutuallyCompatible(asList(VerifyType.USERNAME, VerifyType.PASSWORD)));
    }

    @Test
    void usernameIsNotCompatibleWithAnythingButPassword() {
        for (VerifyType otherType : VerifyType.values()) {
            if (otherType != VerifyType.PASSWORD) {
                assertFalse(VerifyType.mutuallyCompatible(asList(VerifyType.USERNAME, otherType)), "USERNAME+" + otherType);
            }
        }
    }

    @Test
    void passwordIsNotCompatibleWithAnythingButUsername() {
        for (VerifyType otherType : VerifyType.values()) {
            if (otherType != VerifyType.USERNAME) {
                assertFalse(VerifyType.mutuallyCompatible(asList(VerifyType.PASSWORD, otherType)), "PASSWORD+" + otherType);
            }
        }
    }

    @Test
    void nonUsernameIsNotInCompatibleWithNonPassword() {
        for (VerifyType firstType : VerifyType.values()) {
            if (firstType != VerifyType.USERNAME && firstType != VerifyType.PASSWORD) {
                for (VerifyType secondType : VerifyType.values()) {
                    if (secondType != VerifyType.USERNAME && secondType != VerifyType.PASSWORD) {
                        assertFalse(VerifyType.mutuallyCompatible(asList(firstType, secondType)),
                                firstType + "+" + secondType);
                    }
                }
            }
        }
    }
}