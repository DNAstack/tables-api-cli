package org.ga4gh.dataset.cli.test;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.ga4gh.dataset.cli.cmd.Main;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Cmd Tests")
public class TestCmd {

    private static final String API_URL = "https://storage.googleapis.com/ga4gh-dataset-sample";
    private static final String TEST_DATASET_ID = "subjects";

    private static final String EXPECTED_LIST_OUTPUT_FILE = "list/expected_list_output.csv";
    private static final String EXPECTED_GET_OUTPUT_FILE = "get/expected_get_output.csv";
    private static final String CSV_TO_IMPORT = "get/expected_get_output.csv";
    private static final String EXPECTED_IMPORT_OUTPUT="dataset_in_a_bucket/";
    private static final String SCHEMA_TO_IMPORT = "dataset_in_a_bucket/dataset/schema/ca.personalgenomes.schema.Subject";

    private File getTestResource(String relativePathToFile){
        ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource(relativePathToFile).getFile());
        assertTrue(file.exists());
        return file;
    }

    private String getTestResourceAsString(String relativePathToFile){
        try {
            return Files.readString(getTestResource(relativePathToFile).toPath(), StandardCharsets.UTF_8);
        }catch(IOException ex){
            throw new UncheckedIOException(ex);
        }
    }

    private String runCommand(String... args) {
        ByteArrayOutputStream capturedStdoutBytes = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedStdoutBytes));
        int code = Main.runCommand(args);
        if(code != 0){
            System.err.println("Failed to run command " + StringUtils.join(args, " "));
            System.err.println("Output: ");
            System.err.println(capturedStdoutBytes.toString(StandardCharsets.UTF_8));
            throw new RuntimeException("Failed to run command");
        }
        return capturedStdoutBytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Listing datasets works")
    void TestListDatasets(){
        String capturedStdout = runCommand("list", "--username", "", "--password", "", "--api-url", API_URL, "-o", "csv");

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        String expectedOutput = getTestResourceAsString(EXPECTED_LIST_OUTPUT_FILE);
        assertEquals(expectedOutput, capturedStdout);

    }

    @Test
    @DisplayName("Get dataset works")
    void TestGetDataset(){
        String capturedStdout = runCommand("get", "--username", "", "--password", "", "--api-url", API_URL, "-I", TEST_DATASET_ID, "-o", "csv");

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        String expectedOutput = getTestResourceAsString(EXPECTED_GET_OUTPUT_FILE);
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
    void TestImportCSV(){
        Path outputDirPath = null;
        try {
            File csvToImport = getTestResource(CSV_TO_IMPORT);
            File schemaToImport = getTestResource(SCHEMA_TO_IMPORT);

            outputDirPath = Files.createTempDirectory("testImportCsv_" + RandomStringUtils.randomAlphanumeric(12),
                                                           PosixFilePermissions.asFileAttribute((PosixFilePermissions.fromString(
                                                                   "rwx------"))));

            String capturedStdout = runCommand("import",
                                               "--username",
                                               "",
                                               "--password",
                                               "",
                                               "--api-url",
                                               API_URL,
                                               "-I",
                                               TEST_DATASET_ID,
                                               "-i",
                                               csvToImport.toString(),
                                               "-is",
                                               schemaToImport.toString(),
                                               "-o",
                                               outputDirPath.toString());

            verifyDirsAreEqual((getTestResource(EXPECTED_IMPORT_OUTPUT)).toPath(), outputDirPath);
        }catch(IOException ie){
            throw new UncheckedIOException(ie);
        }finally{
            try {
                if (outputDirPath != null) {
                    FileUtils.deleteDirectory(outputDirPath.toFile());
                    if(false){throw new IOException("");} //TEMPORARY!
                }
            }catch(IOException ie){
                throw new UncheckedIOException(ie);
            }
        }
    }
}
