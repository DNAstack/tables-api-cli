package org.ga4gh.dataset.cli.util.outputter;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.ga4gh.dataset.cli.ga4gh.Dataset;

import java.util.List;

public class JsonOutputter extends FormattedOutputter {

    private List<String> propertyKeys;
    private boolean firstPage = true;

    public JsonOutputter() {
    }

    @Override
    public String outputHeader(Dataset page) {
        return "[";
    }

    @Override
    public String outputRows(Dataset page) {
        assertPropertyConsistency(page);
        String propertyListAsJson = page.getObjects().stream()
                .map(this::getPropertyAsJson)
                .reduce((p1,p2)->p1+","+p2).get();
        return propertyListAsJson;
    }

    @Override
    public String outputFooter(Dataset page) {
        return "]";
    }

    private String getPropertyAsJson(Object property){
        try {
            return objectMapper.writeValueAsString(property);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
