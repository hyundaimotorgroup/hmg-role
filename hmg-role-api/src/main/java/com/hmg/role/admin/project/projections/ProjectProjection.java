package com.hmg.role.admin.project.projections;

import java.time.ZonedDateTime;

public interface ProjectProjection {
    String getName();

    String getKey();

    String getDescription();

    String getCompany();

    String getOperatingCountry();

    Boolean getPersonalDataSelfHandled();

    String getServiceConsentHistoryUrl();

    ZonedDateTime getCreatedAt();

    String getCreatedBy();

    ZonedDateTime getUpdatedAt();

    String getUpdatedBy();
}
