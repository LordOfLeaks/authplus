package me.lordofleaks.authplus.core.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.lordofleaks.authplus.core.session.Session;

import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
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
    /**
     * Whether this account is present in the database.
     */
    private boolean registered;

    public Account(@JsonProperty("uniqueId") UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public byte[] getPassword() {
        return password == null ? null : password.clone();
    }

    public byte[] getSalt() {
        return salt == null ? null : salt.clone();
    }

    public void setPassword(byte[] password) {
        this.password = password == null ? null : password.clone();
    }

    public void setSalt(byte[] salt) {
        this.salt = salt == null ? null : salt.clone();
    }

}