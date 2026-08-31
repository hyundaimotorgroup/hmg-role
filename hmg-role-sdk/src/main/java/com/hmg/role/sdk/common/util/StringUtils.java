package com.hmg.role.sdk.common.util;

public class StringUtils {

    public static boolean isBlank(CharSequence s) {
        return !isNotBlank(s);
    }

    public static boolean isNotBlank(CharSequence s) {
        if (s != null) {
            int l = s.length();
            for (int i = 0; i < l; i++) {
                if (!Character.isWhitespace(s.charAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasText(CharSequence s) {
        return isNotBlank(s);
    }
}
