package com.hmg.role.admin.project.interfaces;

import com.hmg.role.admin.project.dto.CreateProjectDto;
import com.hmg.role.admin.project.dto.DeleteBulkProjectDto;
import com.hmg.role.admin.project.dto.ProjectDto;
import com.hmg.role.admin.project.dto.UpdateBulkProjectDto;
import com.hmg.role.util.dto.ListResponseDto;
import com.hmg.role.util.dto.PageRequestWithKeywordDto;
import java.util.List;

public interface ProjectService {
    ProjectDto createProject(CreateProjectDto createProjectDto);

    ListResponseDto<ProjectDto> createBulkProjects(List<CreateProjectDto> createProjectDtoList);

    ListResponseDto<ProjectDto> getAllProjects(PageRequestWithKeywordDto pageRequestDto);

    ProjectDto getProjectByKey(String projectKey);

    ListResponseDto<ProjectDto> updateBulkProjects(
            List<UpdateBulkProjectDto> updateBulkProjectDtoList);

    void deleteProject(String projectKey);

    void deleteBulkProjects(DeleteBulkProjectDto deleteBulkProjectDto);
}
