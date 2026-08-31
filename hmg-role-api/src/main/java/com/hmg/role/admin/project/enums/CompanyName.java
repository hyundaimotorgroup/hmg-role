package com.hmg.role.admin.project.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum CompanyName {
    HYUNDAI("Hyundai"),
    KIA("Kia");

    @JsonValue public final String value;

    CompanyName(String value) {
        this.value = value;
    }

    private static final Map<String, CompanyName> VALUE_MAP =
            Arrays.stream(CompanyName.values())
                    .collect(Collectors.toMap((k -> k.value), Function.identity()));

    private static final Map<String, CompanyName> VALUE_LOWERCASE_MAP =
            Arrays.stream(CompanyName.values())
                    .collect(Collectors.toMap((k -> k.value.toLowerCase()), Function.identity()));

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CompanyName of(String value) {
        if (VALUE_MAP.containsKey(value)) {
            return VALUE_MAP.get(value);
        } else if (VALUE_LOWERCASE_MAP.containsKey(value.toLowerCase())) {
            return VALUE_LOWERCASE_MAP.get(value.toLowerCase());
        } else {
            throw new IllegalArgumentException("Company name not found");
        }
    }

    public static String[] valuesAsString() {
        return Arrays.stream(values()).map(p -> p.value).toArray(String[]::new);
    }

    public static String getFrom(String name) {
        return of(name.toLowerCase()).value;
    }
}
