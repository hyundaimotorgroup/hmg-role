package com.hmg.role.api.admin;

import static com.hmg.role.util.Constants.MAX_LIST_SIZE;

import com.hmg.role.admin.project.dto.CreateProjectDto;
import com.hmg.role.admin.project.dto.DeleteBulkProjectDto;
import com.hmg.role.admin.project.dto.ProjectDto;
import com.hmg.role.admin.project.dto.UpdateBulkProjectDto;
import com.hmg.role.admin.project.dto.UpdateProjectDto;
import com.hmg.role.admin.project.interfaces.ProjectService;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.dto.PageRequestWithKeywordDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Project")
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/projects")
@RestController
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "Create New Project")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDto createNewProject(@RequestBody @Valid CreateProjectDto createProjectDto) {
        return projectService.createProject(createProjectDto);
    }

    @Operation(summary = "Bulk Create New Projects")
    @PostMapping(params = "multiple=true")
    @ResponseStatus(HttpStatus.CREATED)
    public ListResponseDto<ProjectDto> createNewBulkProjects(
            @Parameter(name = "multiple", required = true) Boolean multiple, // for swagger ui
            @RequestBody @Valid @NotEmpty @Size(max = MAX_LIST_SIZE)
                    List<@Valid @NotNull CreateProjectDto> createProjectDtoList) {
        return projectService.createBulkProjects(createProjectDtoList);
    }

    @Operation(summary = "List Projects")
    @GetMapping
    public ListResponseDto<ProjectDto> listProjects(
            @ParameterObject @ModelAttribute @Valid PageRequestWithKeywordDto paginationDto) {
        return projectService.getAllProjects(paginationDto);
    }

    @Operation(summary = "Get Project by Project Key")
    @GetMapping("/{projectKey}")
    public ProjectDto getProjectByProjectKey(@PathVariable String projectKey) {
        return projectService.getProjectByKey(projectKey);
    }

    @Operation(summary = "Update Existing Project")
    @PutMapping("/{projectKey}")
    public ProjectDto updateExistingProject(
            @PathVariable String projectKey,
            @RequestBody @Valid UpdateProjectDto updateProjectDto) {
        UpdateBulkProjectDto convert =
                new UpdateBulkProjectDto(
                        projectKey,
                        updateProjectDto.name(),
                        updateProjectDto.description(),
                        updateProjectDto.company(),
                        updateProjectDto.operatingCountry(),
                        updateProjectDto.personalDataSelfHandled(),
                        updateProjectDto.serviceConsentHistoryUrl());
        return projectService.updateBulkProjects(List.of(convert)).results().getFirst();
    }

    @Operation(summary = "Bulk Update Existing Projects")
    @PutMapping(params = "multiple=true")
    public ListResponseDto<ProjectDto> updateBulkExistingProjects(
            @Parameter(name = "multiple", required = true) Boolean multiple,
            @RequestBody @Valid @NotEmpty @Size(max = MAX_LIST_SIZE)
                    List<@Valid @NotNull UpdateBulkProjectDto> updateBulkProjectDtoList) {
        return projectService.updateBulkProjects(updateBulkProjectDtoList);
    }

    @Operation(summary = "Delete Existing Project")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{projectKey}")
    public void deleteExistingProject(@PathVariable String projectKey) {
        projectService.deleteProject(projectKey);
    }

    @Operation(summary = "Bulk Delete Existing Projects")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(params = "multiple=true")
    public void deleteBulkProjects(
            @Parameter(name = "multiple", required = true) Boolean multiple,
            @RequestBody @Valid DeleteBulkProjectDto deleteBulkProjectDto) {
        projectService.deleteBulkProjects(deleteBulkProjectDto);
    }
}
