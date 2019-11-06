package com.dnastack.ga4gh.tables.cli.util;

import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

public class ExceptionHandler implements IExecutionExceptionHandler {

    @Override
    public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) throws Exception {
        System.err.println("ERROR: " + ex.getMessage());
        System.err.println();
        LoggedCommand command = commandLine.getCommand();
        if (command.getLoggingOptions().isDebug()) {
            ex.printStackTrace();
        }
        return 1;
    }
}
