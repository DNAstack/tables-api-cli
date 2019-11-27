package com.dnastack.ga4gh.tables.cli.output;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.output.format.CSVOutFormatter;
import com.dnastack.ga4gh.tables.cli.output.format.JsonOutputFormatter;
import com.dnastack.ga4gh.tables.cli.output.format.PrettyPrintOutputFormatter;
import com.dnastack.ga4gh.tables.cli.output.format.TableFormatter;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.csv.CSVFormat;

public class OutputTableFormatter implements Closeable {


    private TableFormatter formattedOutputter;
    private boolean firstPage = true;

    public OutputTableFormatter(OutputOptions.OutputMode outputMode, OutputStream outputStream) {
        switch (outputMode) {
            case JSON:
                this.formattedOutputter = new JsonOutputFormatter(outputStream);
                break;
            case CSV:
                this.formattedOutputter = new CSVOutFormatter(CSVFormat.DEFAULT, outputStream);
                break;
            case TSV:
                this.formattedOutputter = new CSVOutFormatter(CSVFormat.TDF, outputStream);
                break;
            case TABLE:
                this.formattedOutputter = new PrettyPrintOutputFormatter(outputStream);
                break;
            default:
                throw new RuntimeException("No supported outputter found.");
        }
    }

    public void write(TableData data) {
        try {
            if (firstPage) {
                this.formattedOutputter.outputHeader(data);
                firstPage = false;
            }
            this.formattedOutputter.outputRows(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void write(Table table) {
        try {
            this.formattedOutputter.outputInfo(table);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(ListTableResponse listTableResponse) {
        try {
            this.formattedOutputter.outputTableList(listTableResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void close() {
        if (this.formattedOutputter != null) {
            this.formattedOutputter.close();
        }
    }
}
