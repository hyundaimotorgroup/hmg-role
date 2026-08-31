package com.hmg.role.util;

import com.hmg.admin.model.UserInfoVo;
import com.hmg.role.abac.scope.AbacScope;
import com.hmg.role.admin.member.Member;
import com.hmg.role.admin.project.Project;
import com.hmg.role.rbac.scope.Scope;
import lombok.Data;

@Data
public class AuthorRequestScope {

    private Member member;
    private UserInfoVo hmgAdminUserInfo;
    private Project project;
    private Cache<Scope> defaultScopeRbac;
    private Cache<AbacScope> defaultScopeAbac;

    public String getMemberKey() {
        return member == null ? hmgAdminUserInfo.getUserId() : member.getKey();
    }

    public Project getProject() {
        if (project == null && member != null) {
            return member.getProject();
        }
        return project;
    }
}
