package me.lordofleaks.authplus.core.account;

public interface AccountValidator {

    /**
     * Checks whether or not given account name can be used to create an account.
     *
     * @param accountName Account name to be checked.
     * @return {@code true} if account name is valid, otherwise {@code false}.
     */
    boolean isAccountNameValid(String accountName);

}