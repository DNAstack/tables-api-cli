package com.dnastack.ga4gh.tables.cli.publisher;

import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;

//TODO: How did I end up here? This can't be right.
public class NoopPublisher extends Publisher {

    public NoopPublisher() {
        super(null, null);
    }

    @Override
    public void publish(Table dataset) {

    }

    @Override
    public void publish(TableData dataset, int pageNum) {

    }

    @Override
    String getObjectRoot(String destination) {
        return null;
    }
}
