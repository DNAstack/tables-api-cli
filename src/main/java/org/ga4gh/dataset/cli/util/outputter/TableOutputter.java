package org.ga4gh.dataset.cli.util.outputter;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import org.ga4gh.dataset.cli.ga4gh.Dataset;

import java.util.*;

public class TableOutputter extends FormattedOutputter {

    private AsciiTable asciiTable;

    TableOutputter() {
        CWC_LongestLine cwc = new CWC_LongestLine();
        cwc.add(4, 100);
        asciiTable = new AsciiTable();
        asciiTable.getContext().setWidth(120);
        asciiTable.getRenderer().setCWC(cwc);
        asciiTable.addRule();
    }

    @Override
    public void outputHeader(Dataset page, StringBuilder output) {
        assertPropertyConsistency(page);
        asciiTable.addRow(propertyKeys).setPaddingLeftRight(1);
        asciiTable.addRule();
        output.append(asciiTable.render()).append(String.format("%n"));
    }

    @Override
    public void outputRows(Dataset page, StringBuilder output) {
        assertPropertyConsistency(page);
        for (Map<String, Object> object : page.getObjects()) {
            List<String> row = getRow(propertyKeys, object);
            asciiTable.addRow(row).setPaddingLeftRight(1);
        }
        String[] asciiRows = asciiTable.renderAsArray();
        String[] rows = Arrays.copyOfRange(asciiRows, 3, asciiRows.length);
        output.append(String.join(String.format("%n"), rows));
    }

    @Override
    public void outputFooter(Dataset page, StringBuilder output) {
        asciiTable.addRule();
        output.append(String.format("%n"));
    }
}
