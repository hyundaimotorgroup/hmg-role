package com.hmg.role.admin.project;

import com.hmg.role.admin.project.dto.CreateProjectDto;
import com.hmg.role.admin.project.dto.ProjectDto;
import com.hmg.role.admin.project.dto.UpdateBulkProjectDto;
import com.hmg.role.admin.project.dto.UpdateProjectDto;
import com.hmg.role.admin.project.projections.ProjectProjection;
import com.hmg.role.common.config.CommonMapperConfig;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CommonMapperConfig.class)
public abstract class ProjectMapper {

    Project toProject(CreateProjectDto projectDto, String memberKey) {
        String key = projectDto.key();
        String name = projectDto.name();
        String description = projectDto.description();
        String companyName = projectDto.company().value;
        String operatingCountry = projectDto.operatingCountry().value;
        boolean personalDataSelfHandled = projectDto.personalDataSelfHandled();
        String serviceConsentHistory = projectDto.serviceConsentHistoryUrl();

        return toProject(
                memberKey,
                key,
                name,
                description,
                companyName,
                operatingCountry,
                personalDataSelfHandled,
                serviceConsentHistory);
    }

    void toProject(Project project, UpdateProjectDto projectDto, String memberKey) {
        String name = projectDto.name();
        String description = projectDto.description();
        String companyName = projectDto.company().value;
        String operatingCountry = projectDto.operatingCountry().value;
        boolean personalDataSelfHandled = projectDto.personalDataSelfHandled();
        String serviceConsentHistory = projectDto.serviceConsentHistoryUrl();

        toProject(
                project,
                memberKey,
                name,
                description,
                companyName,
                operatingCountry,
                personalDataSelfHandled,
                serviceConsentHistory);
    }

    void toProject(Project project, UpdateBulkProjectDto projectDto, String memberKey) {
        String name = projectDto.name();
        String description = projectDto.description();
        String companyName = projectDto.company().value;
        String operatingCountry = projectDto.operatingCountry().value;
        boolean personalDataSelfHandled = projectDto.personalDataSelfHandled();
        String serviceConsentHistory = projectDto.serviceConsentHistoryUrl();

        toProject(
                project,
                memberKey,
                name,
                description,
                companyName,
                operatingCountry,
                personalDataSelfHandled,
                serviceConsentHistory);
    }

    @Mapping(expression = "java(project.getCompany())", target = "company")
    @Mapping(expression = "java(project.getOperatingCountry())", target = "operatingCountry")
    @Mapping(source = "project.personalDataSelfHandled", target = "personalDataSelfHandled")
    @Mapping(source = "project.serviceConsentHistoryUrl", target = "serviceConsentHistoryUrl")
    public abstract ProjectDto toProjectDto(Project project);

    @Mapping(
            expression = "java(getPersonalDataSelfHandled(projection))",
            target = "personalDataSelfHandled")
    abstract ProjectDto toProjectDto(ProjectProjection projection);

    boolean getPersonalDataSelfHandled(ProjectProjection project) {
        return Optional.ofNullable(project.getPersonalDataSelfHandled()).orElse(false);
    }

    private static Project toProject(
            String memberKey,
            String key,
            String name,
            String description,
            String companyName,
            String operatingCountry,
            boolean personalDataSelfHandled,
            String serviceConsentHistory) {
        Project project = new Project();
        project.setKey(key);
        project.setName(name);
        project.setDescription(description);
        project.setCompany(companyName);
        project.setOperatingCountry(operatingCountry);
        project.setPersonalDataSelfHandled(personalDataSelfHandled);
        project.setServiceConsentHistoryUrl(serviceConsentHistory);
        project.setCreatedBy(memberKey);
        project.setCreatedAt(ZonedDateTime.now());
        project.setUpdatedBy(memberKey);
        project.setUpdatedAt(ZonedDateTime.now());
        return project;
    }

    private void toProject(
            Project project,
            String memberKey,
            String name,
            String description,
            String companyName,
            String operatingCountry,
            boolean personalDataSelfHandled,
            String serviceConsentHistory) {
        project.setName(name);
        project.setDescription(description);
        project.setCompany(companyName);
        project.setOperatingCountry(operatingCountry);
        project.setPersonalDataSelfHandled(personalDataSelfHandled);
        project.setServiceConsentHistoryUrl(serviceConsentHistory);
        project.setUpdatedBy(memberKey);
    }
}
