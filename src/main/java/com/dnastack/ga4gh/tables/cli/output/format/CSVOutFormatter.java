package com.dnastack.ga4gh.tables.cli.output.format;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CSVOutFormatter extends TableFormatter {

    private final CSVPrinter csvOutputter;

    public CSVOutFormatter(CSVFormat format, OutputStream stream) {
        super(stream);
        try {
            this.csvOutputter = new CSVPrinter(new OutputStreamWriter(stream), format.withSkipHeaderRecord().withRecordSeparator(String.format("%n")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void outputHeader(TableData page) throws IOException {
        assertPropertyConsistency(page);
        csvOutputter.printRecord(propertyKeys);
    }

    @Override
    public void outputRows(TableData page) throws IOException {
        assertPropertyConsistency(page);
        for (Map<String, Object> object : page.getData()) {
            List<String> row = getRow(propertyKeys, object);
            csvOutputter.printRecord(row);
        }
    }

    @Override
    public void outputFooter() throws IOException {
        csvOutputter.flush();
    }

    @Override
    public void outputInfo(Table table) throws IOException {
        addFoundKeys(table);
        csvOutputter.printRecord("Table Name", "Description", "Properties");
        String firstProperty = propertyKeys.size() > 0 ? propertyKeys.get(0) : "";
        csvOutputter.printRecord(table.getName(), table.getDescription(), firstProperty);
        if (propertyKeys.size() > 1) {
            for (String property : propertyKeys) {
                csvOutputter.printRecord(null, null, property);
            }
        }
    }

    @Override
    public void outputTableList(ListTableResponse tables) throws IOException {
        csvOutputter.printRecord("Table Name", "Description");

        for (Table table : tables.getTables()) {
            csvOutputter.printRecord(table.getName(), table.getDescription());
        }
    }

    @Override
    public void close() {
        try {
            csvOutputter.flush();
            super.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
