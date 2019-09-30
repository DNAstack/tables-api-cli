package org.ga4gh.dataset.cli;

import lombok.Getter;
import org.ga4gh.dataset.cli.util.GCSPublisher;
import picocli.CommandLine;

//Better name?
@Getter
public class PublishOptions {

    //TODO: Currently both the option and args are pretty tied to GCS, should abstractify at some point.
    @CommandLine.Option(names = {"-ptb","--publish-to-bucket"}, description = "A valid GCS URI of the format gs://{bucket}/{blob}")
    private String publishDestination;

    public GCSPublisher getPublisher(){
        return new GCSPublisher(publishDestination);
    }
}
