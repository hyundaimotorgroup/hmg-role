package com.hmg.role.common.cdc.io;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * A text-only file manager for a configured base directory. - Handles only .txt files using UTF-8.
 * - Prevents path traversal and non-text file names.
 */
@Slf4j
@Getter
public class FileManager {
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    /** Base path for the CDC. Currently a random temporary path */
    private final Path basePath;

    @Builder
    FileManager(String baseWorkingPath) throws IOException {
        if (baseWorkingPath == null || "tmpdir".equals(baseWorkingPath)) {
            baseWorkingPath = System.getProperty("java.io.tmpdir");
        }
        String uuid = UUID.randomUUID().toString();
        String basePathStr = baseWorkingPath + "/hmgrole/hmgrole-cdc-" + uuid;
        this.basePath = Files.createDirectories(Path.of(basePathStr)).toAbsolutePath().normalize();
        log.info("basePath: {}", basePath);
    }

    /** Lists all .txt file names (not full paths) under basePath (non-recursive). */
    public List<String> getFileNames() throws IOException {
        try (Stream<Path> stream = Files.list(basePath)) {
            return stream.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.endsWith(".txt"))
                    .sorted()
                    .toList();
        }
    }

    public byte[] readFile(String fileName) throws IOException {
        Path path = resolveAndValidate(fileName);
        return Files.readAllBytes(path);
    }

    /** Checks whether a .txt file with the given name exists in basePath. */
    public boolean fileExists(String fileName) {
        log.debug("fileExists: fileName: {}", fileName);
        Path path = resolveAndValidate(fileName);
        return Files.isRegularFile(path);
    }

    /**
     * Appends a new line (uses system line separator) to the given file. Creates the file if it
     * does not exist.
     */
    public void append(String fileName, String newLine) throws IOException {
        log.debug("appending file: {}, content: {}", fileName, newLine);
        Objects.requireNonNull(newLine, "newLine must not be null");
        Path path = resolveAndValidate(fileName);
        // Ensure parent exists (basePath is created at init)
        Files.writeString(path, newLine + System.lineSeparator(), CHARSET, CREATE, APPEND);
    }

    /**
     * Deletes ALL lines in the file that contain the given substring (case-sensitive). If no line
     * contains the substring, the file remains unchanged.
     */
    public void deleteLine(String fileName, String... contained) throws IOException {
        log.debug("deleting from file: {}, content: {}", fileName, contained);
        Objects.requireNonNull(contained, "contained must not be null");
        Path path = resolveAndValidate(fileName);
        if (!Files.exists(path)) {
            throw new NoSuchFileException("File not found: " + fileName);
        }

        // Read all lines, filter out matches, rewrite the file.
        List<String> lines = Files.readAllLines(path, CHARSET);
        List<String> filtered =
                lines.stream()
                        .filter(
                                line -> {
                                    boolean containing =
                                            Arrays.stream(contained)
                                                    .map(line::contains)
                                                    .reduce(true, (a, b) -> a && b);
                                    return !containing;
                                })
                        .toList();

        // Only rewrite if something changed to avoid timestamp churn.
        if (!filtered.equals(lines)) {
            Files.write(path, filtered, CHARSET);
        }
    }

    /**
     * Renames a file from oldName to newName within basePath. Fails if oldName does not exist or
     * newName already exists.
     */
    public void rename(String oldName, String newName) throws IOException {
        log.debug("renaming file from: {} to: {}", oldName, newName);
        Path oldPath = resolveAndValidate(oldName);
        Path newPath = resolveAndValidate(newName);

        if (!Files.exists(oldPath)) {
            throw new NoSuchFileException("Source file not found: " + oldPath);
        }
        if (Files.exists(newPath)) {
            throw new FileAlreadyExistsException("Target file already exists: " + newPath);
        }

        // Try atomic move; if FS doesn't support, fall back to replace existing safely.
        try {
            Files.move(oldPath, newPath, ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(oldPath, newPath, REPLACE_EXISTING);
        }
    }

    /**
     * Appends every line from each origin text file into the target text file. - Creates the target
     * file if it does not exist. - Preserves line order across origins (originFiles[0] …
     * originFiles[n]). - Writes each line followed by the system line separator.
     *
     * @param targetFileName the .txt file to append into
     * @param originFileNames one or more .txt files to read from
     * @throws IOException if any I/O error occurs, or if an origin file is missing
     * @throws IllegalArgumentException if names are invalid or target equals any origin
     */
    public void appendAll(String targetFileName, String... originFileNames) throws IOException {
        log.debug("appending all files, target: {}, list from: {}", targetFileName, targetFileName);
        Objects.requireNonNull(targetFileName, "targetFileName must not be null");
        Objects.requireNonNull(originFileNames, "originFileNames must not be null");
        if (originFileNames.length == 0) {
            return; // nothing to append
        }

        // Resolve & validate target
        Path target = resolveAndValidate(targetFileName);

        // Resolve & validate origins
        Path[] origins = new Path[originFileNames.length];
        for (int i = 0; i < originFileNames.length; i++) {
            String name =
                    Objects.requireNonNull(
                            originFileNames[i], "originFileNames[" + i + "] must not be null");
            Path p = resolveAndValidate(name);

            // Disallow appending a file into itself
            if (p.equals(target)) {
                throw new IllegalArgumentException(
                        "Target file must not be one of the origin files: " + name);
            }

            if (!Files.isRegularFile(p)) {
                throw new NoSuchFileException(
                        "Origin file not found or not a regular file: " + name);
            }
            origins[i] = p;
        }

        // Append lines efficiently using streaming I/O
        try (var writer = Files.newBufferedWriter(target, CHARSET, CREATE, APPEND)) {
            String nl = System.lineSeparator();
            for (Path origin : origins) {
                try (var reader = Files.newBufferedReader(origin, CHARSET)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.write(nl);
                    }
                }
            }
            writer.flush();
        }
    }

    /**
     * Deletes the specified .txt file from the base directory.
     *
     * @param fileName the name of the file to delete
     * @throws IOException if the file cannot be deleted
     * @throws NoSuchFileException if the file does not exist
     * @throws IllegalArgumentException if the file name is invalid
     */
    public void delete(String fileName) throws IOException {
        log.debug("deleting file: {}", fileName);
        Path path = resolveAndValidate(fileName);

        if (!Files.exists(path)) {
            throw new NoSuchFileException("File not found: " + fileName);
        }

        Files.delete(path);
    }

    /** Resolves the fileName under basePath and validates the file is accessible */
    private Path resolveAndValidate(String fileName) {
        Objects.requireNonNull(fileName, "fileName must not be null");

        Path resolved = basePath.resolve(fileName);

        if (!Files.exists(resolved.getParent())) {
            log.info("Project currently does not exist: {}, creating", resolved.getParent());
            try {
                Files.createDirectories(resolved.getParent());
            } catch (IOException e) {
                log.error("unable to write to file: {}", resolved);
                throw new RuntimeException(e);
            }
        }

        return resolved;
    }
}
