package com.dnastack.ga4gh.tables.cli.cmd;

import com.dnastack.ga4gh.tables.cli.util.option.AuthOptions;
import picocli.CommandLine.Mixin;

public abstract class AuthorizedCmd extends BaseCmd {

    @Mixin
    protected AuthOptions authOptions;

    @Override
    public void run() {
        authOptions.init();
        super.run();
    }
}
