package com.hmg.admin.model;

public class UserInfoVo {

  public String getUserId() {
    return "UNKNOWN";
  }

  public String getSitePermissions(String siteCode) {
    return "ALLOW";
  }

  public boolean anyPermissions(
      String siteCode,
      String[] auditTrailReadPermissions) {
    return true;
  }

  public boolean isUserSettingIp() {
    return true;
  }
}
