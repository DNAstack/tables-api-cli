package org.ga4gh.dataset.cli.util.outputter;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import org.ga4gh.dataset.cli.ga4gh.Dataset;

import java.util.*;

/**
 * For outputting datasets to the terminal in a "pretty" format (ASCII Table).
 */
public class PrettyPrintDatasetOutputter extends DatasetOutputter {

    private AsciiTable asciiTable;

    PrettyPrintDatasetOutputter() {
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
    }

    @Override
    public void outputRows(Dataset page, StringBuilder output) {
        assertPropertyConsistency(page);
        for (Map<String, Object> object : page.getObjects()) {
            List<String> row = getRow(propertyKeys, object);
            asciiTable.addRow(row).setPaddingLeftRight(1);
        }
    }

    @Override
    /**
     * Because of how the table rendering functions, we can't render until we have all elements.
     * (for sizing concerns). Thus, we render at the end, but this also means that we don't have
     * a outputtable string until all datasets have been processed (bad).
     */
    public void outputFooter(Dataset page, StringBuilder output) {
        asciiTable.addRule();
        output.append(asciiTable.render());
        output.append(String.format("%n"));
    }
}
