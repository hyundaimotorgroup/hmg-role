package com.hmg.role.admin.member.exceptions;

import com.hmg.role.util.exceptions.NotFoundException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class MemberNotFoundException extends NotFoundException {

    private final List<String> memberKeys;

    public MemberNotFoundException(String... memberKeys) {
        this(List.of(memberKeys));
    }

    public MemberNotFoundException(List<String> memberKeys) {
        super("Member Not Found");
        this.memberKeys = memberKeys;
    }

    public String getLogMessage() {
        return MessageFormat.format("Member {0} Not Found", memberKeys);
    }
}
