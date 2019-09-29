package org.ga4gh.dataset.cli.util.outputter;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import org.ga4gh.dataset.cli.ga4gh.Dataset;

import java.util.*;

public class TableOutputter extends FormattedOutputter {
    private AsciiTable asciiTable;

    public TableOutputter() {
        CWC_LongestLine cwc = new CWC_LongestLine();
        cwc.add(4, 100);
        asciiTable = new AsciiTable();
        asciiTable.getContext().setWidth(120);
        asciiTable.getRenderer().setCWC(cwc);
        asciiTable.addRule();
    }

    @Override
    public String outputHeader(Dataset page) {
        asciiTable.addRow(propertyKeys).setPaddingLeftRight(1);
        asciiTable.addRule();
        return asciiTable.render() + String.format("%n");
    }

    @Override
    public String outputRows(Dataset page) {
        assertPropertyConsistency(page);
        for (Map<String, Object> object : page.getObjects()) {
            List<String> row = getRow(propertyKeys, object);
            asciiTable.addRow(row).setPaddingLeftRight(1);
        }
        String[] asciiRows = asciiTable.renderAsArray();
        String[] rows = Arrays.copyOfRange(asciiRows, 3, asciiRows.length);
        return String.join(String.format("%n"), rows);
    }

    @Override
    public String outputFooter(Dataset page) {
        asciiTable.addRule();
        return String.format("%n");
    }
}
