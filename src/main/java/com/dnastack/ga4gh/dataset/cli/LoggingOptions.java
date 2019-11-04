package com.dnastack.ga4gh.dataset.cli;

//import ch.qos.logback.classic.LoggerContext;
//import ch.qos.logback.classic.joran.JoranConfigurator;
//import ch.qos.logback.classic.util.ContextInitializer;
//import ch.qos.logback.core.joran.spi.JoranException;
//import org.slf4j.LoggerFactory;

import picocli.CommandLine.Option;

public class LoggingOptions {

    @Option(names = "--debug", description = "Print DEBUG level information")
    private boolean debug;

    public void setupLogging() {
//            System.setProperty(
//                    ContextInitializer.CONFIG_FILE_PROPERTY,
//                    debug ? "logback-debug.xml" : "logback.xml");
//
//        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
//        JoranConfigurator configurator = new JoranConfigurator();
//        configurator.setContext(context);
//        context.reset();
//
//        try {
//            configurator.doConfigure(this.getClass().getResource(debug ? "/logback-debug.xml" : "/logback.xml"));
//        } catch (JoranException e) {
//            throw new RuntimeException(e);
//        }
    }
}
