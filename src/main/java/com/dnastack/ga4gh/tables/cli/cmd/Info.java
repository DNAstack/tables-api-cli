package com.dnastack.ga4gh.tables.cli.cmd;

import com.dnastack.ga4gh.tables.cli.config.Config;
import com.dnastack.ga4gh.tables.cli.config.ConfigUtil;
import com.dnastack.ga4gh.tables.cli.input.TableFetcher;
import com.dnastack.ga4gh.tables.cli.input.TableFetcherFactory;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.output.OutputWriter;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions;
import java.io.IOException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;

@Command(name = "info", mixinStandardHelpOptions = true, description = "Get table info (*=required argument)", requiredOptionMarker = '*', sortOptions = false)
public class Info extends AuthorizedCmd {

    @Mixin
    private OutputOptions outputOptions;

    @Parameters(index = "0", paramLabel = "tableName", description = "The name of the table")
    private String tableName;


    @Override
    public void runCmd() {
        Config config = ConfigUtil.getUserConfig();
        TableFetcher tableDataFetcher = TableFetcherFactory
            .getTableFetcher(config.getApiUrl(), false, config.getRequestAuthorization());

        if (outputOptions.getDestinationTableName() == null) {
            outputOptions.setDestinationTableName(tableName);
        }

        try (OutputWriter outputWriter = new OutputWriter(outputOptions)) {
            Table info = tableDataFetcher.getInfo(tableName);
            outputWriter.write(info);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
