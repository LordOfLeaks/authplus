package me.lordofleaks.authplus.core.comm.impl.model;

import lombok.Data;
import me.lordofleaks.authplus.core.session.Session;

@Data
public class GetSessionResponse {

    public static final int ID = 2;
    private String reqId;
    private Session session;

}