package com.extremum.sharedmodels.personal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author rpuch
 */
class VerifyTypeTest {
    @Test
    void usernameAndPasswordShouldBeInSameGroup() {
        assertTrue(VerifyType.USERNAME.inSameGroupWith(VerifyType.PASSWORD));
        assertTrue(VerifyType.PASSWORD.inSameGroupWith(VerifyType.USERNAME));
    }

    @Test
    void usernameIsNotInSameGroupWithAnythingButPassword() {
        for (VerifyType otherType : VerifyType.values()) {
            if (otherType != VerifyType.PASSWORD) {
                assertFalse(VerifyType.USERNAME.inSameGroupWith(otherType));
            }
        }
    }

    @Test
    void passwordIsNotInSameGroupWithAnythingButUsername() {
        for (VerifyType otherType : VerifyType.values()) {
            if (otherType != VerifyType.USERNAME) {
                assertFalse(VerifyType.PASSWORD.inSameGroupWith(otherType));
            }
        }
    }

    @Test
    void nonUsernameIsNotInSameGroupAsNonPassword() {
        for (VerifyType firstType : VerifyType.values()) {
            if (firstType != VerifyType.USERNAME && firstType != VerifyType.PASSWORD) {
                for (VerifyType secondType : VerifyType.values()) {
                    if (secondType != VerifyType.USERNAME && secondType != VerifyType.PASSWORD) {
                        assertFalse(firstType.inSameGroupWith(secondType));
                    }
                }
            }
        }
    }
}