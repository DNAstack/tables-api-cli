package org.ga4gh.dataset.cli.util;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.ga4gh.dataset.cli.OutputOptions;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class Outputter implements Closeable {

    private final OutputOptions.OutputMode outputMode;

    private AsciiTable asciiTable;
    private CSVPrinter csvPrinter;


    public void emitHeader(String ...headers){
        emitHeader(Arrays.asList(headers));
    }

    public void emitHeader(List<String> headers){
        try {
            switch (outputMode) {
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
        try {
            if(outputMode == OutputOptions.OutputMode.TABLE){
                    asciiTable.addRule();
                    System.out.println(asciiTable.render());
            }else if (csvPrinter != null) {
                csvPrinter.close();
                csvPrinter = null;
            }
        }catch(IOException ie){
            throw new UncheckedIOException(ie);
        }
    }

    @Override
    public void close(){
        emitFooter();
    }
}
