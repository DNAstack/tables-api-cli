package com.dnastack.ga4gh.tables.cli.cmd;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine.Command;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions
        ;

@Command(name = "test", mixinStandardHelpOptions = true, description = "Test", requiredOptionMarker = '*', sortOptions = false)
public class Test extends AuthorizedCmd {

    private static final String TEST_TABLE_NAME = "subjects";

    private static final String CSV_TO_IMPORT = "get/expected_get_output.csv";
    private static final String EXPECTED_IMPORT_OUTPUT = "tables_in_a_bucket/";
    private static final String EXPECTED_PUBLISH_OUTPUT = "publish/";
    private static final String SCHEMA_TO_IMPORT = "ca.personalgenomes.schemas.Subject";


    private File getTestResource(String relativePathToFile) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource(relativePathToFile).getFile());
        assert(file.exists());
        return file;
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

    @Override
    public void runCmd() {
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
                    "-o",
                    outputDirPath.toString());

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

    public File getResource(String fileURI) {

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileURI);
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return new File(resource.getFile());
        }
    }


}
