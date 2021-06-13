package me.lordofleaks.authplus.core.account.impl;

import me.lordofleaks.authplus.core.account.AccountValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountValidatorImplTest {

    @Test
    void testAccountNameTooShort() {
        AccountValidator validator = new AccountValidatorImpl();
        assertFalse(validator.isAccountNameValid("t"));
        assertFalse(validator.isAccountNameValid("te"));
    }

    @Test
    void testAccountNameTooLong() {
        AccountValidator validator = new AccountValidatorImpl();
        assertFalse(validator.isAccountNameValid("12345678123456781"));
        assertFalse(validator.isAccountNameValid("123456781234567812345"));
    }

    @Test
    void testAccountNameValid() {
        AccountValidator validator = new AccountValidatorImpl();
        assertTrue(validator.isAccountNameValid("_superstar12_"));
        assertTrue(validator.isAccountNameValid("ifacereader11"));
        assertTrue(validator.isAccountNameValid("Joe"));
    }

    @Test
    void testAccountNameIllegalCharacters() {
        AccountValidator validator = new AccountValidatorImpl();
        assertFalse(validator.isAccountNameValid("testAcc-"));
        assertFalse(validator.isAccountNameValid("testAcc@"));
        assertFalse(validator.isAccountNameValid("testAcc!"));
        assertFalse(validator.isAccountNameValid("testAcc$"));
        assertFalse(validator.isAccountNameValid("testAcc("));
        assertFalse(validator.isAccountNameValid("testAcc)"));
    }
}