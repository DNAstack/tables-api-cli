package com.dnastack.ga4gh.tables.cli.util;

import com.dnastack.ga4gh.tables.cli.util.option.LoggingOptions;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import lombok.Getter;

public class LoggingUtil {

    @Getter
    private static boolean debug = false;

    public static void init(LoggingOptions options) {
        debug = options.isDebug();
        try {
            if (options.getStderr() != null && !options.getStderr().isEmpty()) {
                System.setErr(new PrintStream(new FileOutputStream(options.getStderr()), true));
            }

            if (options.getStdout() != null && !options.getStdout().isEmpty()) {
                System.setOut(new PrintStream(new FileOutputStream(options.getStdout()), true));
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
