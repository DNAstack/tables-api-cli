package com.dnastack.ga4gh.tables.cli.cmd;

import com.dnastack.ga4gh.tables.cli.util.LoggedCommand;
import com.dnastack.ga4gh.tables.cli.util.option.AuthOptions;
import com.dnastack.ga4gh.tables.cli.util.option.LoggingOptions;
import lombok.Getter;
import picocli.CommandLine.Mixin;

public abstract class BaseCmd implements LoggedCommand, Runnable {


    @Mixin
    @Getter
    protected LoggingOptions loggingOptions;

    @Mixin
    protected AuthOptions authOptions;

    public abstract void runExceptionally();

    @Override
    public void run() {
        loggingOptions.init();
        authOptions.init();
        runExceptionally();

    }

}
