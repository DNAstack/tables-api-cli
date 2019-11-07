package com.dnastack.ga4gh.tables.cli.cmd;

import com.dnastack.ga4gh.tables.cli.util.LoggingUtil;
import com.dnastack.ga4gh.tables.cli.util.option.LoggingOptions;
import lombok.Getter;
import picocli.CommandLine.Mixin;

public abstract class BaseCmd implements Runnable {


    @Mixin
    @Getter
    protected LoggingOptions loggingOptions;

    @Override
    public void run() {
        LoggingUtil.init(loggingOptions);
        runCmd();
    }

    public abstract void runCmd();

}
