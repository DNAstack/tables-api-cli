package org.ga4gh.dataset.cli.publisher;

import org.ga4gh.dataset.cli.AuthOptions;
import org.ga4gh.dataset.cli.Config;
import org.ga4gh.dataset.cli.ga4gh.Dataset;
import org.ga4gh.dataset.cli.ga4gh.Page;

public abstract class Publisher {

    protected final String destination;
    protected final String blob;
    protected final Config.Auth auth;

    public Publisher(String destination, Config.Auth auth) {
        this.destination = destination;
        this.blob = getBlobName(destination);
        this.auth = auth;
    }

    public abstract void publish(Dataset dataset, int pageNum);

    abstract String getBlobName(String destination);

    public Page getAbsolutePagination(Page oldPagination, int pageNum) {
        final String blobName = this.blob.substring(this.blob.lastIndexOf('/') + 1);
        Page newPagination = new Page();
        if (pageNum != 0) {
            String prevPage = blobName;
            if (pageNum > 1) {
                prevPage += "." + (pageNum - 1);
            }
            newPagination.setPrevPageUrl(prevPage);
        }
        if (oldPagination.getNextPageUrl() != null) {
            String nextPage = String.format("%s.%s", blobName, pageNum + 1);
            newPagination.setNextPageUrl(nextPage);
        }
        return newPagination;
    }
}
