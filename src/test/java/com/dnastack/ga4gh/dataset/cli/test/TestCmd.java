package com.dnastack.ga4gh.dataset.cli.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dnastack.ga4gh.tables.cli.cmd.Main;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

@DisplayName("Cmd Tests")
public class TestCmd {

    private static final String API_URL = "https://storage.googleapis.com/ga4gh-tables-example";
    private static final String TEST_TABLE_NAME = "subjects";

    private static final String EXPECTED_LIST_OUTPUT_FILE = "list/expected_list_output.csv";
    private static final String EXPECTED_GET_OUTPUT_CSV = "get/expected_get_output.csv";
    private static final String EXPECTED_INFO_OUTPUT_CSV = "get/expected_info_output.csv";
    private static final String EXPECTED_GET_OUTPUT_JSON = "get/expected_get_output.json";
    private static final String CSV_TO_IMPORT = "get/expected_get_output.csv";
    private static final String EXPECTED_IMPORT_OUTPUT = "tables_in_a_bucket/";
    private static final String EXPECTED_PUBLISH_OUTPUT = "publish/";
    private static final String SCHEMA_TO_IMPORT = "ca.personalgenomes.schemas.Subject";

    private File getTestResource(String relativePathToFile) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource(relativePathToFile).getFile());
        assertTrue(file.exists());
        return file;
    }

    private String getTestResourceAsString(String relativePathToFile) {
        try {
            return Files.readString(getTestResource(relativePathToFile).toPath(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private String runCommand(String... args) {
        ByteArrayOutputStream capturedStdoutBytes = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedStdoutBytes));
        int code = Main.runCommand(args);
        if (code != 0) {
            System.err.println("Project exited with code: " + code);
            System.err.println("Failed to run command " + StringUtils.join(args, " "));
            System.err.println("Output: ");
            System.err.println(capturedStdoutBytes.toString(StandardCharsets.UTF_8));
            throw new RuntimeException("Failed to run command");
        }
        return capturedStdoutBytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Listing tables works")
    void TestListTables() {
        String capturedStdout = runCommand("list", "--api-url", API_URL, "-o", "csv");
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        String expectedOutput = getTestResourceAsString(EXPECTED_LIST_OUTPUT_FILE);
        assertEquals(expectedOutput, capturedStdout);

    }

    @Test
    @DisplayName("Info table as CSV works")
    void TestGetTableInfoCsv() {
        String capturedStdout = runCommand("info", "--api-url", API_URL, TEST_TABLE_NAME, "-o", "csv");
        String expectedOutput = getTestResourceAsString(EXPECTED_INFO_OUTPUT_CSV);
        assertEquals(expectedOutput, capturedStdout);
    }

    @Test
    @DisplayName("Data table as CSV works")
    void TestGetTableDataCsv() {
        String capturedStdout = runCommand("data", "--api-url", API_URL, TEST_TABLE_NAME, "-o", "csv");
        String expectedOutput = getTestResourceAsString(EXPECTED_GET_OUTPUT_CSV);
        assertEquals(expectedOutput, capturedStdout);
    }

    @Test
    @DisplayName("Info table as JSON works")
    void TestGetTableDataAsJson() {
        String capturedStdout = runCommand("info", "--api-url", API_URL, TEST_TABLE_NAME, "-o", "json");
        String expectedOutput = getTestResourceAsString(EXPECTED_GET_OUTPUT_JSON);
        assertEquals(expectedOutput, capturedStdout);
    }


    private static void verifyDirsAreEqual(Path expected, Path actual) throws IOException {
        Files.walkFileTree(expected, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file,
                BasicFileAttributes attrs)
                throws IOException {
                FileVisitResult result = super.visitFile(file, attrs);

                // get the relative file name from path "expected"
                Path expectedPath = expected.relativize(file);
                // construct the path for the counterpart file in "actual"
                Path actualPath = actual.resolve(expectedPath);

                byte[] actualBytes = Files.readAllBytes(actualPath);
                byte[] expectedBytes = Files.readAllBytes(file);
                JSONAssert.assertEquals(new String(expectedBytes), new String(actualBytes), JSONCompareMode.STRICT);

                return result;
            }
        });
    }

    @Test
    @DisplayName("Import CSV works")
    void TestImportCSV() {
        Path outputDirPath = null;
        try {
            File csvToImport = getTestResource(CSV_TO_IMPORT);
            File schemaToImport = getTestResource(SCHEMA_TO_IMPORT);

            outputDirPath = Files.createTempDirectory("testImportCsv_" + RandomStringUtils.randomAlphanumeric(12),
                PosixFilePermissions.asFileAttribute((PosixFilePermissions.fromString(
                    "rwx------"))));

            String capturedStdout = runCommand("import",
                "-N",
                TEST_TABLE_NAME,
                "-i",
                csvToImport.toString(),
                "-dm",
                schemaToImport.toString(),
                "--description",
                "Sample table describing subjects",
                "-p",
                outputDirPath.toString());

            verifyDirsAreEqual((getTestResource(EXPECTED_IMPORT_OUTPUT)).toPath(), outputDirPath);
        } catch (IOException ie) {
            throw new UncheckedIOException(ie);
        } finally {
            try {
                if (outputDirPath != null) {
                    FileUtils.deleteDirectory(outputDirPath.toFile());
                    if (false) {
                        throw new IOException("");
                    } //TEMPORARY!
                }
            } catch (IOException ie) {
                throw new UncheckedIOException(ie);
            }
        }
    }

    @Test
    @DisplayName("Publish To local file system works")
    void TestPublish() {
        Path outputDirPath = null;
        try {

            outputDirPath = Files.createTempDirectory("testPublis_" + RandomStringUtils.randomAlphanumeric(12),
                PosixFilePermissions.asFileAttribute((PosixFilePermissions.fromString(
                    "rwx------"))));

            runCommand("info", "--api-url", API_URL, TEST_TABLE_NAME, "-p", outputDirPath
                .toString(), "-N", TEST_TABLE_NAME);
            runCommand("data", "--api-url", API_URL, TEST_TABLE_NAME, "-p", outputDirPath
                .toString(), "-N", TEST_TABLE_NAME);
            verifyDirsAreEqual((getTestResource(EXPECTED_PUBLISH_OUTPUT)).toPath(), outputDirPath);
        } catch (IOException ie) {
            throw new UncheckedIOException(ie);
        } finally {
            try {
                if (outputDirPath != null) {
                    FileUtils.deleteDirectory(outputDirPath.toFile());
                    if (false) {
                        throw new IOException("");
                    } //TEMPORARY!
                }
            } catch (IOException ie) {
                throw new UncheckedIOException(ie);
            }
        }
    }
}
