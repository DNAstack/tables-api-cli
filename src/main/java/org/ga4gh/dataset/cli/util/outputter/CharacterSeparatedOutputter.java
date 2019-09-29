package org.ga4gh.dataset.cli.util.outputter;

import org.ga4gh.dataset.cli.ga4gh.Dataset;

import java.util.List;
import java.util.Map;

public class CharacterSeparatedOutputter extends FormattedOutputter {

    private String delimiter;

    public CharacterSeparatedOutputter(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public String outputHeader(Dataset page) {
        String header = String.join(delimiter, propertyKeys) + String.format("%n");
        return header;
    }

    @Override
    public String outputRows(Dataset page) {
        assertPropertyConsistency(page);
        StringBuilder output = new StringBuilder();
        for (Map<String, Object> object : page.getObjects()) {
            List<String> row = getRow(propertyKeys, object);
            output.append(String.join(delimiter, row) + String.format("%n"));
        }
        return output.toString();
    }

    @Override
    public String outputFooter(Dataset page) {
        return "";
    }
}
