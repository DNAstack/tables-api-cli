package org.ga4gh.dataset.cli.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.ClassUtils;
import org.ga4gh.dataset.cli.OutputOptions;
import org.ga4gh.dataset.cli.ga4gh.Dataset;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

@RequiredArgsConstructor
public class Outputter implements Closeable {

    private final OutputOptions.OutputMode outputMode;

    private AsciiTable asciiTable;
    private CSVPrinter csvPrinter;
    private ObjectMapper objectMapper = new ObjectMapper();
    private List<String> propertyKeys;

    public void output(Iterable<Dataset> pages) {
        boolean first = true;
        for (Dataset dataset : pages) {
            if (propertyKeys == null) {
                propertyKeys = new ArrayList<>(dataset.getSchema().getPropertyMap().keySet());
                emitHeader(propertyKeys);
            } else {
                assertSchemaPropertyKeysAreSameASFirstPagePropertyKeys(dataset.getSchema().getPropertyMap());
            }
            if(this.outputMode == OutputOptions.OutputMode.JSON){
                System.out.print(first ? "[" : ",");
                first = false;
                String propertyListAsJson = dataset.getObjects().stream()
                        .map(this::getPropertyAsJson)
                        .reduce((p1,p2)->p1+","+p2).get();
                System.out.print(propertyListAsJson);
            }else {
                for (Map<String, Object> object : dataset.getObjects()) {
                    emitLine(getRow(object));
                }
            }
        }
        emitFooter();
        this.close();
    }

    private void assertSchemaPropertyKeysAreSameASFirstPagePropertyKeys(LinkedHashMap<String, Object> propertyMapToTest){
        Set<String> foundKeys = propertyMapToTest.keySet();
        if(!propertyKeys.containsAll(foundKeys)){
            throw new IllegalArgumentException("Unexpected schema properties found on a page that do not match schema on first page");
        }else if(!foundKeys.containsAll(propertyKeys)){
            throw new IllegalArgumentException("Missing schema properties on page that were present in first page schema.");
        }
    }

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
        switch (outputMode) {
            case TABLE:
                asciiTable.addRule();
                System.out.println(asciiTable.render());
                break;
            case JSON:
                System.out.println("]");
                break;
            default:
                break;
        }
    }

    private String getPropertyAsJson(Object property){
        try {
            return objectMapper.writeValueAsString(property);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private List<String> getRow(Map<String, Object> object){
        List<String> outputLine = new ArrayList<>(propertyKeys.size());
        for(String key : propertyKeys){
            Object value = object.get(key);
            if(value == null) {
                outputLine.add("");
            }else if((value instanceof String) || (ClassUtils.isPrimitiveOrWrapper(value.getClass()))){
                outputLine.add(value.toString());
            }else{
                try {

                    outputLine.add(objectMapper.writeValueAsString(value));
                }catch(JsonProcessingException jpe){
                    throw new IllegalArgumentException(jpe);
                }
            }
        }
        return outputLine;
    }

    @Override
    public void close(){
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
