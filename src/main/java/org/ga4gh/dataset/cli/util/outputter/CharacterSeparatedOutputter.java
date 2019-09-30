package org.ga4gh.dataset.cli.util.outputter;

import org.ga4gh.dataset.cli.ga4gh.Dataset;

import java.util.List;
import java.util.Map;

public class CharacterSeparatedOutputter extends FormattedOutputter {

    private String delimiter;

    CharacterSeparatedOutputter(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public void outputHeader(Dataset page, StringBuilder output) {
        assertPropertyConsistency(page);
        String header = String.join(delimiter, propertyKeys) + String.format("%n");
        output.append(header);
    }

    @Override
    public void outputRows(Dataset page, StringBuilder output) {
        assertPropertyConsistency(page);
        StringBuilder rows = new StringBuilder();
        for (Map<String, Object> object : page.getObjects()) {
            List<String> row = getRow(propertyKeys, object);
            rows.append(String.join(delimiter, row) + String.format("%n"));
        }
        output.append(rows);
    }

    @Override
    public void outputFooter(Dataset page, StringBuilder output) {
        return;
    }
}
