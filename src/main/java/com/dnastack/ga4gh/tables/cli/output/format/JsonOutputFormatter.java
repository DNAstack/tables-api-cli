package com.dnastack.ga4gh.tables.cli.output.format;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class JsonOutputFormatter extends TableFormatter {


    private JsonGenerator generator;
    private boolean closeObject = true;

    public JsonOutputFormatter(OutputStream outputStream) {
        super(outputStream);
        JsonFactory factory = new JsonFactory();
        try {
            generator = factory.createGenerator(outputStream);
            generator.setCodec(new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void outputHeader(TableData page) throws IOException {
        generator.writeStartObject();
        if (page.getDataModel() != null) {
            generator.writeObjectField("data_model", page.getDataModel());
        }
        generator.writeArrayFieldStart("data");
    }

    @Override
    public void outputRows(TableData page) throws IOException {
        assertPropertyConsistency(page);
        for (Map<String, Object> data : page.getData()) {
            generator.writeObject(data);
        }
    }

    @Override
    public void outputFooter() throws IOException {
        generator.writeEndArray();
        generator.writeEndObject();
        generator.flush();
    }

    @Override
    public void outputInfo(Table table) throws IOException {
        generator.writeObject(table);
        generator.flush();
        closeObject = false;
    }

    @Override
    public void outputTableList(ListTableResponse table) throws IOException {
        generator.writeObject(table);
        generator.flush();
        closeObject = false;
    }

    @Override
    public void close() {
        if (closeObject) {
            super.close();
        }
    }
}
