package org.ga4gh.dataset.cli;

import lombok.Getter;
import org.ga4gh.dataset.cli.publisher.ABSPublisher;
import org.ga4gh.dataset.cli.publisher.GCSPublisher;
import org.ga4gh.dataset.cli.publisher.NoPublisher;
import org.ga4gh.dataset.cli.publisher.Publisher;
import picocli.CommandLine;

//Better name?
@Getter
public class PublishOptions {

    //TODO: Currently both the option and args are pretty tied to GCS, should abstractify at some point.
    @CommandLine.Option(names = {"-ptb","--publish-to-bucket"}, description = "A valid GCS URI of the format gs://{bucket}/{blob}")
    private String publishDestination;

    public Publisher getPublisher(Config.Auth auth){
        if (publishDestination == null || publishDestination.isBlank()) {
            return new NoPublisher();
        }
        if (!publishDestination.startsWith("gs://")) {
            return new ABSPublisher(publishDestination, auth);
        }
        return new GCSPublisher(publishDestination, auth);
    }
}
