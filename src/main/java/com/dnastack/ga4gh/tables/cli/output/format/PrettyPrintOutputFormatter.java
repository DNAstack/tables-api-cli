package com.dnastack.ga4gh.tables.cli.output.format;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestWordMax;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * For outputting datasets to the terminal in a "pretty" format (ASCII Table).
 */
public class PrettyPrintOutputFormatter extends TableFormatter {

    private AsciiTable asciiTable;

    public PrettyPrintOutputFormatter(OutputStream outputStream) {
        super(outputStream);
        CWC_LongestWordMax cwc = new CWC_LongestWordMax(80);
        asciiTable = new AsciiTable();
        asciiTable.getContext().setWidth(120);
        asciiTable.getRenderer().setCWC(cwc);
        asciiTable.addRule();
    }

    @Override
    public void outputHeader(TableData page) {
        assertPropertyConsistency(page);
        asciiTable.addRow(propertyKeys).setPaddingLeftRight(3);
    }

    @Override
    public void outputRows(TableData page) {
        assertPropertyConsistency(page);
        for (Map<String, Object> object : page.getData()) {
            List<String> row = getRow(propertyKeys, object).stream().map(v -> v == null ? "" : v)
                .collect(Collectors.toList());
            asciiTable.addRule();
            asciiTable.addRow(row).setPaddingLeftRight(3);
        }
    }

    @Override
    public void outputFooter() throws IOException {
        asciiTable.addRule();
        outputStream.write(asciiTable.render().getBytes());
        outputStream.write(String.format("%n").getBytes());
    }

    @Override
    public void outputInfo(Table table) throws IOException {
        addFoundKeys(table);
        asciiTable.addRow("Table Name", "Description", "Properties").setPaddingLeftRight(3);
        asciiTable.addRule();

        String name = table.getName() == null ? "" : table.getName();
        String description = table.getDescription() == null ? "" : table.getDescription();
        String firstProperty = propertyKeys.size() > 0 ? propertyKeys.get(0) : "";
        asciiTable.addRow(name, description, firstProperty).setPaddingLeftRight(3);

        if (propertyKeys.size() > 1) {
            for (int i = 1; i < propertyKeys.size(); i++) {
                asciiTable.addRule();
                String property = propertyKeys.get(i) == null ? "" : propertyKeys.get(i);
                asciiTable.addRow("", "", property).setPaddingLeftRight(3);
            }
        }
    }

    @Override
    public void outputTableList(ListTableResponse tables) throws IOException {
        asciiTable.addRow("Table Name", "Description").setPaddingLeftRight(3);

        for (Table table : tables.getTables()) {
            asciiTable.addRule();
            String name = table.getName() == null ? "" : table.getName();
            String description = table.getDescription() == null ? "" : table.getDescription();
            asciiTable.addRow(name, description).setPaddingLeftRight(3);
        }
    }
}
