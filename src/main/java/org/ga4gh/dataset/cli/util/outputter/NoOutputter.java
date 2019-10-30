package org.ga4gh.dataset.cli.util.outputter;

import org.ga4gh.dataset.cli.ga4gh.Dataset;

//TODO: Dear code reader. Even as I write this I know I'm making a terrible, terrible mistake.
// Forgive me now for the sins you are about to witness.
public class NoOutputter extends DatasetOutputter {

    @Override
    protected void outputHeader(Dataset page, StringBuilder output) {
        return;
    }

    @Override
    protected void outputRows(Dataset page, StringBuilder output) {
        return;
    }

    @Override
    protected void outputFooter(Dataset page, StringBuilder output) {
        return;
    }
}
