package com.hmg.role.sdk.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE) // disable instantiations
public final class Utils {
    // this class isn't supposed to be in the SDK module
    // need to be moved somewhere like hmg-role-common
    // TODO

    /**
     * Check whether {@code others} are equal to {@code a}
     *
     * @param a String to be compared against
     * @param others String to be compared to
     * @return true if and only if {@code a} is equal to each element of {@code others}
     */
    public static boolean isAllEqual(String a, String... others) {
        for (String o : others) {
            if (!StringUtils.equals(a, o)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Given a list of patterns, check if the string contains any of them
     *
     * @param theList the pattern list
     * @param theString the string to check against
     * @return true if the string is contained in any one of the list, false otherwise
     */
    public static boolean containsIgnoreCase(Collection<String> theList, String theString) {
        return theList.stream().anyMatch(p -> theString.toLowerCase().contains(p.toLowerCase()));
    }

    public static String sha256(String input) {
        return sha256(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String sha256(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input);

            StringBuilder hexString = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm is not supported", e);
        }
    }

    public static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    public static final DateTimeFormatter ISO_OFFSET_DATE_TIME_FORMATTER =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public static ZonedDateTime parseIsoDate(String date) {
        // assume the date is in UTC since there's no hour information passed
        return ZonedDateTime.ofInstant(
                LocalDate.parse(date, ISO_DATE_FORMATTER).atTime(0, 0, 0),
                ZoneOffset.UTC,
                ZoneId.of("UTC"));
    }

    public static ZonedDateTime parseIsoDateTime(String date) {
        return ZonedDateTime.parse(date, ISO_OFFSET_DATE_TIME_FORMATTER);
    }

    public static String formatToIso8601String(ZonedDateTime time) {
        if (time == null) {
            return "";
        } else {
            return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(time);
        }
    }

    public static String formatToIso8601String(Instant t) {
        return formatToIso8601String(t, ChronoUnit.SECONDS);
    }

    public static String formatToIso8601String(Instant t, ChronoUnit resolution) {
        return DateTimeFormatter.ISO_INSTANT.format(t.truncatedTo(resolution));
    }

    public static String formatToIso8601String(Instant t, ZoneId zone) {
        return ZonedDateTime.ofInstant(t.truncatedTo(ChronoUnit.SECONDS), zone)
                .format(ISO_OFFSET_DATE_TIME_FORMATTER);
    }

    public static String formatToIso8601String(Instant t, ZoneOffset offset) {
        return OffsetDateTime.ofInstant(t.truncatedTo(ChronoUnit.SECONDS), offset)
                .format(ISO_OFFSET_DATE_TIME_FORMATTER);
    }
}
