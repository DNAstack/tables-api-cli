package com.dnastack.ga4gh.tables.cli.util.outputter;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * For outputting datasets to the terminal in a "pretty" format (ASCII Table).
 */
public class PrettyPrintDatasetOutputter extends TableOutputter {

    private AsciiTable asciiTable;

    public PrettyPrintDatasetOutputter(OutputStream outputStream) {
        super(outputStream);
        CWC_LongestLine cwc = new CWC_LongestLine();
        cwc.add(4, 100);
        asciiTable = new AsciiTable();
        asciiTable.getContext().setWidth(120);
        asciiTable.getRenderer().setCWC(cwc);
        asciiTable.addRule();
    }

    @Override
    protected void outputHeader(TableData page) {
        assertPropertyConsistency(page);
        asciiTable.addRow(propertyKeys).setPaddingLeftRight(3);
        asciiTable.addRule();
    }

    @Override
    protected void outputRows(TableData page) {
        assertPropertyConsistency(page);
        for (Map<String, Object> object : page.getData()) {
            List<String> row = getRow(propertyKeys, object).stream().map(v -> v == null ? "" : v)
                .collect(Collectors.toList());
            asciiTable.addRow(row).setPaddingLeftRight(3);
        }
    }

    @Override
    protected void outputFooter(TableData page) throws IOException {
        asciiTable.addRule();
        outputStream.write(asciiTable.render().getBytes());
        outputStream.write(String.format("%n").getBytes());
    }

    @Override
    protected void outputInfo(Table table) throws IOException {
        addFoundKeys(table);
        asciiTable.addRow("Table Name", "Description", "Properties").setPaddingLeftRight(3);
        asciiTable.addRule();

        String firstProperty = propertyKeys.size() > 0 ? propertyKeys.get(0) : "";
        asciiTable.addRow(table.getName(), table.getDescription(), firstProperty).setPaddingLeftRight(3);

        if (propertyKeys.size() > 1) {
            for (int i = 1; i < propertyKeys.size(); i++) {
                asciiTable.addRule();
                asciiTable.addRow("", "", propertyKeys.get(i)).setPaddingLeftRight(3);
            }
        }
        asciiTable.addRule();
        outputStream.write(asciiTable.render().getBytes());
        outputStream.write(String.format("%n").getBytes());
    }

    @Override
    protected void outputTableList(ListTableResponse tables) throws IOException {
        asciiTable.addRow("Table Name", "Description").setPaddingLeftRight(3);
        asciiTable.addRule();

        for (Table table : tables.getTables()) {
            asciiTable.addRow(table.getName(), table.getDescription());
            asciiTable.addRule();
        }
        outputStream.write(asciiTable.render().getBytes());
        outputStream.write(String.format("%n").getBytes());

    }
}
