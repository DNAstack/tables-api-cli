package org.ga4gh.dataset.cli.util.outputter;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.ga4gh.dataset.cli.ga4gh.Dataset;

public class JsonDatasetOutputter extends DatasetOutputter {

    JsonDatasetOutputter() {
    }

    @Override
    public void outputHeader(Dataset page, StringBuilder output) {
        output.append("[");
    }

    @Override
    public void outputRows(Dataset page, StringBuilder output) {
        assertPropertyConsistency(page);
        String propertyListAsJson = page.getObjects().stream()
                .map(this::getPropertyAsJson)
                .reduce((p1,p2)->p1+","+p2).get();
        output.append(propertyListAsJson);
        output.append(",");
    }

    @Override
    public void outputFooter(Dataset page, StringBuilder output) {
        output.deleteCharAt(output.length() - 1);
        output.append("]");
    }

    private String getPropertyAsJson(Object property){
        try {
            return objectMapper.writeValueAsString(property);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
