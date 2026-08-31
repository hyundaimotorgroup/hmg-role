package com.hmg.role.sdk.rbac.permission;

import com.hmg.role.sdk.common.exception.NotFoundException;
import com.hmg.role.sdk.rbac.permission.dto.*;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatRequestByRole;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatRequestByUser;
import com.hmg.role.sdk.rbac.permission.dto.PermissionFlatResponse;
import com.hmg.role.sdk.rbac.permission.dto.PermissionResponse;
import com.hmg.role.sdk.rbac.permission.dto.ResourceRequest;
import com.hmg.role.sdk.rbac.permission.dto.RoleSubjectRequest;
import com.hmg.role.sdk.rbac.permission.dto.UserSubjectRequest;
import java.util.Collection;

public interface PermissionService {

    Collection<? extends PermissionResponse> getPermissions(
            UserSubjectRequest userSubject, Collection<? extends ResourceRequest> resources)
            throws NotFoundException;

    Collection<? extends PermissionResponse> getPermissions(
            RoleSubjectRequest roleSubject, Collection<? extends ResourceRequest> resources)
            throws NotFoundException;

    Collection<? extends PermissionFlatResponse> getPermissionsFlattened(
            PermissionFlatRequestByRole request) throws NotFoundException;

    Collection<? extends PermissionFlatResponse> getPermissionsFlattened(
            PermissionFlatRequestByUser request) throws NotFoundException;

    Collection<? extends PermissionFlatResponse> getPermissionsFlattened(
            UserSubjectRequest roleSubject, Collection<? extends ResourceRequest> resources)
            throws NotFoundException;

    Collection<? extends PermissionFlatResponse> getPermissionsFlattened(
            RoleSubjectRequest roleSubject, Collection<? extends ResourceRequest> resources)
            throws NotFoundException;
}
