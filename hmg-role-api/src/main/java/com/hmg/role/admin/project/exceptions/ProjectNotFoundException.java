package com.hmg.role.admin.project.exceptions;

import com.hmg.role.util.exceptions.NotFoundException;
import java.text.MessageFormat;
import java.util.List;
import lombok.Getter;

@Getter
public class ProjectNotFoundException extends NotFoundException {

    private final List<String> projectKeys;

    public ProjectNotFoundException(String... projectKeys) {
        this(List.of(projectKeys));
    }

    public ProjectNotFoundException(List<String> projectKeys) {
        super("Project Not Found");
        this.projectKeys = projectKeys;
    }

    public static ProjectNotFoundException ofApiKey(String apiKey) {
        return new ProjectNotFoundException(
                String.format("Project with API key: %s not found", apiKey));
    }

    public String getLogMessage() {
        return MessageFormat.format("Project {0} Not Found", projectKeys);
    }
}
