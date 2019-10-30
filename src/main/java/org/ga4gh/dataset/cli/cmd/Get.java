package org.ga4gh.dataset.cli.cmd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ga4gh.dataset.cli.AuthOptions;
import org.ga4gh.dataset.cli.Config;
import org.ga4gh.dataset.cli.OutputOptions;
import org.ga4gh.dataset.cli.PublishOptions;
import org.ga4gh.dataset.cli.ga4gh.Dataset;
import org.ga4gh.dataset.cli.util.ConfigUtil;
import org.ga4gh.dataset.cli.util.ContextUtil;
import org.ga4gh.dataset.cli.util.DatasetFetcher;
import org.ga4gh.dataset.cli.publisher.Publisher;
import org.ga4gh.dataset.cli.util.outputter.Outputter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "get", description = "Get dataset (*=required argument)", requiredOptionMarker='*', sortOptions = false)
public class Get implements Runnable {

//    @Mixin private LoggingOptions loggingOptions;
    @Mixin private OutputOptions outputOptions;
    @Mixin private AuthOptions authOptions;
    @Mixin private PublishOptions publishOptions;

    @Option(
            names = {"-I", "--dataset-id", "--id"},
            description = "Dataset ID",
            required = true)
    private String datasetId;

    @Option(names = {"--dataset-endpoint"},
            description = "Dataset endpoint (this argument will be deprecated soon)",
            required=false)
    private String datasetEndpoint;

    @Option(names = {"--access-token"},
            description = "Custom access token",
            required=false)
    private String customAccessToken;

    @Override
    public void run() {
        //loggingOptions.setupLogging();
        authOptions.initAuth();
        Config.Auth auth = ConfigUtil.getUserConfig().getAuth();
        final ObjectMapper jsonMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        String accessToken = customAccessToken == null ? ContextUtil.getAccessToken() : customAccessToken;
        DatasetFetcher datasetFetcher = new DatasetFetcher(datasetId, false, accessToken);
        if(datasetEndpoint != null){
            datasetFetcher.setDatasetEndpoint(datasetEndpoint);
        }
        Outputter outputter = outputOptions.getOutputter();
        Publisher publisher = publishOptions.getPublisher(auth);
        int pageNum = 0;
        for (Dataset dataset : datasetFetcher.getPages()) {
            String pageOutput = outputter.output(dataset, pageNum == 0);
            publisher.publish(dataset, pageNum);
            System.out.print(pageOutput);
            pageNum++;
        }
    }
}
