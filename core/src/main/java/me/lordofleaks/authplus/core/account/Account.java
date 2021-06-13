package me.lordofleaks.authplus.core.account;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class Account {

    /**
     * The unique id of this account.
     */
    private final UUID uniqueId;
    /**
     * The name of this account.
     */
    private String name;
    /**
     * The password of this account.
     */
    private byte[] password;
    /**
     * The salt of this account's password.
     */
    private byte[] salt;
    /**
     * Whether this account was registered by a premium user.
     */
    private boolean registeredPremium;

    //Return copy to avoid modifying
    public byte[] getPassword() {
        return password.clone();
    }

    //Return copy to avoid modifying
    public byte[] getSalt() {
        return salt.clone();
    }
}