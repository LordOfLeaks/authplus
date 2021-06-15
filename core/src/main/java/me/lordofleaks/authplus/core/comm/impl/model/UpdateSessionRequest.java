package me.lordofleaks.authplus.core.comm.impl.model;

import lombok.Data;
import me.lordofleaks.authplus.core.session.Session;

@Data
public class UpdateSessionRequest {

    public static final int ID = 3;
    private Session session;
    private boolean updateAccount;

}