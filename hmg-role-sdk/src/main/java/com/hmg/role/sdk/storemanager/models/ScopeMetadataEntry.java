package com.hmg.role.sdk.storemanager.models;

import com.hmg.role.sdk.rbac.permission.model.ScopeModel;
import java.time.OffsetDateTime;

public interface ScopeMetadataEntry extends ScopeModel {
    OffsetDateTime getLastUpdatedAt();
}
