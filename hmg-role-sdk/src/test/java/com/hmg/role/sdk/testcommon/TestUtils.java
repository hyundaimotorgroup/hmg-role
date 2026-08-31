package com.hmg.role.sdk.testcommon;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public final class TestUtils {
    public static final Instant END_OF_TIME_INSTANT =
            LocalDate.ofEpochDay(365241780471L).atStartOfDay(ZoneId.of("UTC")).toInstant();
    public static final ZonedDateTime END_OF_TIME =
            ZonedDateTime.ofInstant(END_OF_TIME_INSTANT, ZoneId.of("UTC"));
    public static final String END_OF_TIME_STR =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(END_OF_TIME);

    private TestUtils() {
        throw new UnsupportedOperationException();
    }

    public static byte[] readFile(String fileName) throws IOException {
        URL resource =
                Objects.requireNonNull(TestUtils.class.getClassLoader().getResource(fileName));
        Path path = new File(resource.getPath()).toPath();
        return Files.readAllBytes(path);
    }
}
