package me.lordofleaks.authplus.core.session;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;

@Getter
@Setter
@RequiredArgsConstructor
public class Session {

    /**
     * Name of the session holder.
     */
    private final String accountName;
    /**
     * Whether this session was created by user who first joined the server with premium account.
     */
    private boolean registeredPremium;
    /**
     * Whether this session was created by user whose name is associated with any premium account.
     */
    private boolean playerNamePremium;
    /**
     * Whether this session was authorised either by login or premium account.
     */
    private boolean authorised;
    /**
     * Whether the session holder is registered.
     */
    private boolean registered;
    /**
     * Password hash of the session holder.
     */
    private byte[] accountPassword;
    /**
     * Password salt of the session holder.
     */
    private byte[] salt;

}