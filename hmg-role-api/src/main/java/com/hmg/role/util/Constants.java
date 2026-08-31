package com.hmg.role.util;

import static lombok.AccessLevel.PRIVATE;

import java.util.Set;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class Constants {

    public static final String X_HMG_ROLE_API_KEY = "X-HMG-ROLE-API-KEY";
    public static final String X_HMG_ROLE_PROJECT_KEY = "X-HMG-ROLE-PROJECT-KEY";
    public static final String DELETED = "deleted";
    public static final String DELETED_DATE_FORMAT = "uuuuMMddHHmmss";

    public static final String HTTP_URI_REGEX_PATTERN = "^https?://.+";
    public static final String ALPHA_DASH_UNDERSCORE_REGEX_PATTERN = "^[a-zA-Z0-9_\\-.]+$";

    public static final String REQUEST_DTO_URI_REGEX_PATTERN = "^[a-zA-Z0-9_\\-./=:?{}]+$";
    public static final String ABAC_DTO_KEY_REGEX_PATTERN = REQUEST_DTO_URI_REGEX_PATTERN;

    public static final int MAX_LIST_SIZE = 100; // max size of a list in a request
    public static final int MAX_500_SIZE = 500; // max size of a list resource dataType in a request
    public static final int MAX_255_SIZE = 255;
    public static final int MAX_100_SIZE = 100; // max length of resource type key
    public static final int MAX_50_SIZE = 50; // max length of User key and name
    public static final int MAX_40_SIZE = 40;
    public static final int MAX_10_SIZE = 10; // max number of entries in metadata

    public static final boolean CASCADE_DISABLED = false;
    public static final boolean CASCADE_ENABLED = true;

    public static final String DEFAULT_SCOPE_KEY = "default_scope";

    public static final String WILDCARD = "*";

    public static final String MDC_KEY_TRACE_ID = "traceId";
    public static final String MDC_KEY_PROJECT_KEY = "projectKey";
    public static final String MDC_KEY_MEMBER_KEY = "memberKey";
    public static final String MDC_KEY_API_KEY = "apiKey";
    public static final String MDC_KEY_USER_IP = "userIp";

    public static final String NOT_AVAILABLE = "N/A";

    public static final Set<String> EXEMPTED_REQUEST_PATH_ENDPOINT =
            Set.of("audit-trails", "access-keys");
}
