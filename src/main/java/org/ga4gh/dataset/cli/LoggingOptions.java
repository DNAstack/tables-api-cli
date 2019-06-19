package org.ga4gh.dataset.cli;

import ch.qos.logback.classic.util.ContextInitializer;
import picocli.CommandLine.Option;

public class LoggingOptions {

    @Option(names = "--debug", description = "Print DEBUG level information")
    private boolean debug;

    public void setupLogging() {
        System.setProperty(
                ContextInitializer.CONFIG_FILE_PROPERTY,
                debug ? "logback-debug.xml" : "logback.xml");
    }
}
