package com.dnastack.ga4gh.dataset.cli.cmd;

import com.dnastack.ga4gh.dataset.cli.AuthOptions;
import com.dnastack.ga4gh.dataset.cli.Config;
import com.dnastack.ga4gh.dataset.cli.OutputOptions;
import com.dnastack.ga4gh.dataset.cli.PublishOptions;
import com.dnastack.ga4gh.dataset.cli.ga4gh.Dataset;
import com.dnastack.ga4gh.dataset.cli.util.ConfigUtil;
import com.dnastack.ga4gh.dataset.cli.util.ContextUtil;
import com.dnastack.ga4gh.dataset.cli.util.DatasetSearcher;
import com.dnastack.ga4gh.dataset.cli.publisher.Publisher;
import com.dnastack.ga4gh.dataset.cli.util.outputter.Outputter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "query", description = "Query dataset (*=required argument)", requiredOptionMarker='*', sortOptions = false)
public class Query implements Runnable {

//    @Mixin private LoggingOptions loggingOptions;
    @Mixin private OutputOptions outputOptions;
    @Mixin private AuthOptions authOptions;
    @Mixin private PublishOptions publishOptions;

    @Option(
            names = {"-q", "--query"},
            description = "SQL search query",
            required = true)
    private String query;

    @Override
    public void run() {
//        loggingOptions.setupLogging();
        //authOptions.initAuth();
        Config.Auth auth = ConfigUtil.getUserConfig().getAuth();
        String accessToken = ContextUtil.getAccessToken();
        DatasetSearcher datasetSearcher = new DatasetSearcher(query, false, accessToken);
        Outputter outputter = outputOptions.getOutputter();
        Publisher publisher = publishOptions.getPublisher(auth);
        int pageNum = 0;
        for (Dataset dataset : datasetSearcher.getPages()) {
            String pageOutput = outputter.output(dataset, pageNum == 0);
            publisher.publish(dataset, pageNum);
            System.out.print(pageOutput);
            pageNum++;
        }
    }
}
