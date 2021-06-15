package me.lordofleaks.authplus.core.session;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import me.lordofleaks.authplus.core.account.Account;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@EqualsAndHashCode
@Getter
@Setter
public class Session {

    /**
     * The session holder.
     */
    @NotNull
    private final Account account;
    /**
     * Whether this session was authorized either by login or premium account.
     */
    private boolean authorized;

    public Session(@JsonProperty("account") @NotNull Account account) {
        this.account = account;
    }
}