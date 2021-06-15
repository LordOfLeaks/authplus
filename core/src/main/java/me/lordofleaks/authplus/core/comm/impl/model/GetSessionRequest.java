package me.lordofleaks.authplus.core.comm.impl.model;

import lombok.Data;

import java.util.UUID;

@Data
public class GetSessionRequest {

    public static final int ID = 1;
    private String reqId;
    private UUID accountId;

}