package com.dnastack.ga4gh.tables.cli.output.publish;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.output.OutputTableFormatter;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions.OutputMode;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

public class ConsolePublisher extends AbstractPublisher implements Closeable {

    private final OutputStream outputStream;
    private final OutputTableFormatter outputWriter;

    public ConsolePublisher(OutputMode outputMode, String tableName, String destination) {
        super(outputMode, tableName, destination);
        outputStream = System.out;
        outputWriter = new OutputTableFormatter(this.outputMode, outputStream);
    }

    @Override
    public void publish(Table table) {
        if (!tableName.equals(table.getName())) {
            table.setName(tableName);
        }
        outputWriter.write(table);
    }

    @Override
    public void publish(ListTableResponse table) {
        outputWriter.write(table);
    }

    @Override
    public void publish(TableData tableData, int pageNum) {
        TableData newData = new TableData(tableData.getDataModel(), tableData.getData(), null);
        outputWriter.write(newData);
    }

    @Override
    public String getObjectRoot(String destination) {
        return null;
    }

    @Override
    public void close() throws IOException {
        outputWriter.close();
    }
}
