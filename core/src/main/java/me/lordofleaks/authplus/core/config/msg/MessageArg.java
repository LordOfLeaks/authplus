package me.lordofleaks.authplus.core.config.msg;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(staticName = "of")
@Getter
public class MessageArg {

    private final String name;
    private final String value;

}