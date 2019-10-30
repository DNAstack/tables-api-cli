package org.ga4gh.dataset.cli.util.outputter;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.ga4gh.dataset.cli.OutputOptions;
import org.ga4gh.dataset.cli.ga4gh.Dataset;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

public class Outputter implements Closeable {

    private final OutputOptions.OutputMode outputMode;

    private AsciiTable asciiTable;
    private CSVPrinter csvPrinter;
    private DatasetOutputter formattedOutputter;

    public Outputter(OutputOptions.OutputMode outputMode) {
        this.outputMode = outputMode;
        switch (outputMode) {
            case JSON:
                this.formattedOutputter = new JsonDatasetOutputter();
                break;
            case CSV:
                this.formattedOutputter = new CharacterSeparatedDatasetOutputter(",");
                break;
            case TSV:
                this.formattedOutputter = new CharacterSeparatedDatasetOutputter("\t");
                break;
            case TABLE:
                this.formattedOutputter = new PrettyPrintDatasetOutputter();
                break;
            case SILENT:
                this.formattedOutputter = new NoOutputter();
                break;
            default:
                throw new RuntimeException("No supported outputter found.");
        }
    }

    public String output(Dataset dataset, boolean firstPage) {
        StringBuilder output = new StringBuilder();
        if (firstPage) {
            this.formattedOutputter.outputHeader(dataset, output);
        }

        this.formattedOutputter.outputRows(dataset, output);
        if (dataset.getPagination() == null || dataset.getPagination().getNextPageUrl() == null) {
            this.formattedOutputter.outputFooter(dataset, output);
        }

        return output.toString();
    }

    public void emitHeader(String ...headers){
        emitHeader(Arrays.asList(headers));
    }

    public void emitHeader(List<String> headers){
        try {
            switch (outputMode) {
                case JSON:
                    System.out.print("[");
                case TSV:
                    //System.out.println(String.join("\t", headers));
                    this.csvPrinter = new CSVPrinter(System.out, CSVFormat.TDF.withHeader(headers.toArray(new String[headers.size()])));
                    break;
                case CSV:
                    //System.out.println(String.join(",", headers));
                    this.csvPrinter = new CSVPrinter(System.out, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])));
                    //csvPrinter.h
                    break;
                case TABLE:
                    CWC_LongestLine cwc = new CWC_LongestLine();
                    cwc.add(4, 100);
                    asciiTable = new AsciiTable();
                    asciiTable.getContext().setWidth(120);
                    asciiTable.getRenderer().setCWC(cwc);
                    asciiTable.addRule();
                    asciiTable.addRow(headers).setPaddingLeftRight(1);
                    asciiTable.addRule();
                    break;
            }
        }catch(IOException ex){
            throw new UncheckedIOException(ex);
        }
    }

    public void emitLine(String ...cells){
        emitLine(Arrays.asList(cells));
    }

    public void emitLine(List<String> line) {
        try {
            switch (outputMode) {
                case TSV:
                case CSV:
                    csvPrinter.printRecord(line);
                    break;
                case TABLE:
                    asciiTable.addRow(line).setPaddingLeftRight(1);
                    break;
            }
        }catch(IOException ie){
            throw new UncheckedIOException(ie);
        }
    }

    private void emitFooter(){
        switch (outputMode) {
            case TABLE:
                asciiTable.addRule();
                System.out.println(asciiTable.render());
                break;
            case JSON:
                System.out.print("]");
                break;
            default:
                break;
        }
    }

    @Override
    public void close(){
        emitFooter();
        try {
            if (csvPrinter != null) {
                csvPrinter.close();
                csvPrinter = null;
            }
        } catch(IOException ie){
            throw new UncheckedIOException(ie);
        }
    }
}
