package com.dnastack.ga4gh.tables.cli.util.outputter;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.ClassUtils;

public abstract class TableOutputter implements Closeable {

    protected ObjectMapper objectMapper = new ObjectMapper();
    protected List<String> propertyKeys;
    protected final OutputStream outputStream;

    protected abstract void outputHeader(TableData page) throws IOException;

    protected abstract void outputRows(TableData page) throws IOException;

    protected abstract void outputFooter(TableData page) throws IOException;

    protected abstract void outputInfo(Table table) throws IOException;

    protected abstract void outputTableList(ListTableResponse table) throws IOException;


    public TableOutputter(OutputStream stream) {
        this.outputStream = stream;
    }

    protected void assertPropertyConsistency(TableData tableData) {
        Set<String> foundKeys = ((Map<String, ?>) tableData.getDataModel().get("properties")).keySet();
        addFoundKeys(foundKeys);
        if (!propertyKeys.containsAll(foundKeys)) {
            throw new IllegalArgumentException("Unexpected schema properties found on a page that do not match schema on first page");
        } else if (!foundKeys.containsAll(propertyKeys)) {
            throw new IllegalArgumentException("Missing schema properties on page that were present in first page schema.");
        }
    }

    protected void addFoundKeys(Table table){
        Set<String> foundKeys = ((Map<String, ?>) table.getDataModel().get("properties")).keySet();
        addFoundKeys(foundKeys);
    }

    protected void addFoundKeys(Set<String> foundKeys) {
        if (propertyKeys == null) {
            propertyKeys = new ArrayList<>();
            propertyKeys.addAll(foundKeys);
            return;
        }
    }

    protected List<String> getRow(List<String> propertyKeys, Map<String, Object> object) {
        List<String> outputLine = new ArrayList<>(propertyKeys.size());
        for (String key : propertyKeys) {
            Object value = object.get(key);
            if (value == null) {
                outputLine.add(null);
            } else if ((value instanceof String) || (ClassUtils.isPrimitiveOrWrapper(value.getClass()))) {
                outputLine.add(value.toString());
            } else {
                try {

                    outputLine.add(objectMapper.writeValueAsString(value));
                } catch (JsonProcessingException jpe) {
                    throw new IllegalArgumentException(jpe);
                }
            }
        }
        return outputLine;
    }


    @Override
    public void close() {
        try {
            this.outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
