package com.dnastack.ga4gh.dataset.cli.publisher;

import com.dnastack.ga4gh.dataset.cli.ga4gh.Dataset;

//TODO: How did I end up here? This can't be right.
public class NoPublisher extends Publisher {

    public NoPublisher() {
        super(null, null);
    }

    @Override
    public void publish(Dataset dataset, int pageNum) {
        return;
    }

    @Override
    String getBlobName(String destination) {
        return null;
    }
}
