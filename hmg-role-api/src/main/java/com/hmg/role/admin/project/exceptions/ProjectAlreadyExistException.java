package com.hmg.role.admin.project.exceptions;

import com.hmg.role.util.exceptions.AlreadyExistException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class ProjectAlreadyExistException extends AlreadyExistException {

    private final List<String> projectKeys;

    public ProjectAlreadyExistException(String... projectKeys) {
        this(List.of(projectKeys));
    }

    public ProjectAlreadyExistException(List<String> projectKeys) {
        super("Project Already Exist");
        this.projectKeys = projectKeys;
    }

    public String getLogMessage() {
        return MessageFormat.format("Project {0} Already Exist", projectKeys);
    }
}
