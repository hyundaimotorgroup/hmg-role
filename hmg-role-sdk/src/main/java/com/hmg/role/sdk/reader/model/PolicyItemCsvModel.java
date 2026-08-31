package com.hmg.role.sdk.reader.model;

import com.hmg.role.sdk.common.enums.Effect;
import com.hmg.role.sdk.rbac.permission.model.PolicyItemModel;
import com.hmg.role.sdk.rbac.permission.model.UserModel;
import com.opencsv.bean.CsvBindByName;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class PolicyItemCsvModel implements PolicyItemModel, UserModel, Serializable {
    // not using record here because OpenCSV might not be able to recognize it
    // role_key,scope_key,action_name,resource_type,effect,project_key,user_key

    @CsvBindByName(column = "role_key")
    private String roleKey;

    @CsvBindByName(column = "scope_key")
    private String scopeKey;

    @CsvBindByName(column = "action_name")
    private String actionName;

    @CsvBindByName(column = "resource_type_key")
    private String resourceType;

    @CsvBindByName(column = "effect")
    private String effect;

    @CsvBindByName(column = "project_key")
    private String projectKey;

    @CsvBindByName(column = "user_key")
    private String userKey;

    @Override
    public String getActionName() {
        return actionName;
    }

    @Override
    public String getResourceTypeKey() {
        return resourceType;
    }

    @Override
    public String getRoleKey() {
        return roleKey;
    }

    @Override
    public String getScopeKey() {
        return scopeKey;
    }

    @Override
    public Effect getEffect() {
        return Effect.valueOf(effect);
    }

    @Override
    public String getUserKey() {
        return userKey;
    }
}
