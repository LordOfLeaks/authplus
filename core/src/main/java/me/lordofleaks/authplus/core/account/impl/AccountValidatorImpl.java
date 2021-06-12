package me.lordofleaks.authplus.core.account.impl;

import me.lordofleaks.authplus.core.account.AccountValidator;

import java.util.regex.Pattern;

public class AccountValidatorImpl implements AccountValidator {

    private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]{3,16}");

    @Override
    public boolean isAccountNameValid(String accountName) {
        return NAME_PATTERN.matcher(accountName).matches();
    }
}