package com.dnastack.ga4gh.tables.cli.cmd;

import com.dnastack.ga4gh.tables.cli.util.LoggingUtil;
import com.dnastack.ga4gh.tables.cli.util.option.LoggingOptions;
import lombok.Getter;
import lombok.SneakyThrows;
import picocli.CommandLine.Mixin;

import java.io.IOException;

public abstract class BaseCmd implements Runnable {


    @Mixin
    @Getter
    protected LoggingOptions loggingOptions;

    @SneakyThrows
    @Override
    public void run() {
        LoggingUtil.init(loggingOptions);
        runCmd();
    }

    public abstract void runCmd() throws IOException;

}
