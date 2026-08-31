package com.hmg.role.admin.member.exceptions;

import com.hmg.role.util.exceptions.AlreadyExistException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class MemberAlreadyExistException extends AlreadyExistException {

    private final List<String> memberKeys;

    public MemberAlreadyExistException(String... memberKeys) {
        this(List.of(memberKeys));
    }

    public MemberAlreadyExistException(List<String> memberKeys) {
        super("Member Already Exist");
        this.memberKeys = memberKeys;
    }

    public String getLogMessage() {
        return MessageFormat.format("Member {0} Already Exist", memberKeys);
    }
}
