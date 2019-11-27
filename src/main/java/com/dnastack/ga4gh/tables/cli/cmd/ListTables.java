package com.dnastack.ga4gh.tables.cli.cmd;

import com.dnastack.ga4gh.tables.cli.config.Config;
import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.input.TableFetcher;
import com.dnastack.ga4gh.tables.cli.input.TableFetcherFactory;
import com.dnastack.ga4gh.tables.cli.output.OutputWriter;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions;
import java.io.IOException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "list", mixinStandardHelpOptions = true, description = "List Tables", requiredOptionMarker = '*', sortOptions = false)
public class ListTables extends AuthorizedCmd {

    @Mixin
    private OutputOptions outputOptions;


    @Override
    public void runCmd() {

        outputOptions.setDestinationTableName("");

        Config config = ConfigUtil.getUserConfig();
        TableFetcher tableDataFetcher = TableFetcherFactory
            .getTableFetcher(config.getApiUrl(), false, config.getRequestAuthorization());
        try (OutputWriter outputWriter = new OutputWriter(outputOptions)) {
            outputWriter.write(tableDataFetcher.list());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
