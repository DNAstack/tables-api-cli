package com.dnastack.ga4gh.tables.cli.util.outputter;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.dnastack.ga4gh.tables.cli.util.option.OutputOptions;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.csv.CSVFormat;

public class Outputter implements Closeable {


    private TableOutputter formattedOutputter;

    public Outputter(OutputOptions.OutputMode outputMode, OutputStream outputStream) {
        switch (outputMode) {
            case JSON:
                this.formattedOutputter = new JsonOutputter(outputStream);
                break;
            case CSV:
                this.formattedOutputter = new CSVOutputter(CSVFormat.DEFAULT, outputStream);
                break;
            case TSV:
                this.formattedOutputter = new CSVOutputter(CSVFormat.TDF, outputStream);
                break;
            case TABLE:
                this.formattedOutputter = new PrettyPrintDatasetOutputter(outputStream);
                break;
            case SILENT:
                this.formattedOutputter = new NoopOutputter(outputStream);
                break;
            default:
                throw new RuntimeException("No supported outputter found.");
        }
    }

    public void output(TableData data, boolean firstPage) {
        try {
            if (firstPage) {
                this.formattedOutputter.outputHeader(data);
            }

            this.formattedOutputter.outputRows(data);
            if (data.getPagination() == null || data.getPagination().getNextPageUrl() == null) {
                this.formattedOutputter.outputFooter(data);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void output(Table table) {
        try {
            this.formattedOutputter.outputInfo(table);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void output(ListTableResponse listTableResponse) {
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
