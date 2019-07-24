package com.extremum.sharedmodels.personal;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author rpuch
 */
class VerifyTypeTest {
    private final List<VerifyType> grouplessVerifyTypes = Arrays.asList(
            VerifyType.EMAIL, VerifyType.EMAIL_VERIFY, VerifyType.SMS, VerifyType.SMS_VERIFY);
    
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
        for (VerifyType otherType : grouplessVerifyTypes) {
            assertFalse(VerifyType.mutuallyCompatible(asList(VerifyType.USERNAME, otherType)), "USERNAME+" + otherType);
        }
    }

    @Test
    void passwordIsNotCompatibleWithAnythingButUsername() {
        for (VerifyType otherType : grouplessVerifyTypes) {
            assertFalse(VerifyType.mutuallyCompatible(asList(VerifyType.PASSWORD, otherType)), "PASSWORD+" + otherType);
        }
    }

    @Test
    void nonUsernameIsNotInCompatibleWithNonPassword() {
        for (VerifyType firstType : grouplessVerifyTypes) {
            for (VerifyType secondType : grouplessVerifyTypes) {
                assertFalse(VerifyType.mutuallyCompatible(asList(firstType, secondType)),
                        firstType + "+" + secondType);
            }
        }
    }
}