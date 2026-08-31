package com.hmg.role.common.keymanagement;

import com.hmg.role.util.container.Pair;
import java.time.Duration;
import java.time.Period;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyManagementUtils {
    public static final Duration CLIENT_IDENTITY_RENEWAL_GRACE_PERIOD = Duration.ofDays(2);
    public static final Duration CLIENT_IDENTITY_EXPIRY_GRACE_PERIOD = Duration.ofDays(1);

    public static final String SECRET_PROPERTY_KEY_NAME = "secret-property";

    public static Pair<Period, Duration> parseIsoPeriod(String isoString) {
        if (isoString == null || isoString.isBlank()) {
            throw new IllegalArgumentException("ISO period string cannot be null or blank");
        }

        // Ensure it starts with 'P'
        if (!isoString.startsWith("P")) {
            throw new IllegalArgumentException("Invalid ISO period format: " + isoString);
        }

        String datePart = isoString;
        String timePart = "";

        int tIndex = isoString.indexOf('T');
        if (tIndex != -1) {
            datePart = isoString.substring(0, tIndex);
            timePart = isoString.substring(tIndex); // includes 'T'
        }

        Period period = Period.ZERO;
        Duration duration = Duration.ZERO;

        if (!datePart.equals("P")) { // "P" alone means no date part
            period = Period.parse(datePart);
        }

        if (!timePart.isEmpty()) {
            duration =
                    Duration.parse(
                            "PT" + timePart.substring(1)); // Ensure proper format for Duration
        }

        return new Pair<>(period, duration);
    }
}
