package com.dnastack.ga4gh.dataset.cli.util.outputter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ClassUtils;
import com.dnastack.ga4gh.dataset.cli.ga4gh.Dataset;

import java.util.*;

public abstract class DatasetOutputter {
    protected ObjectMapper objectMapper = new ObjectMapper();
    protected List<String> propertyKeys;
    protected abstract void outputHeader(Dataset page, StringBuilder output);
    protected abstract void outputRows(Dataset page, StringBuilder output);
    protected abstract void outputFooter(Dataset page, StringBuilder output);

    protected void assertPropertyConsistency(Dataset dataset){
        Set<String> foundKeys = dataset.getSchema().getPropertyMap().keySet();
        if (propertyKeys == null) {
            propertyKeys = new ArrayList<>();
            propertyKeys.addAll(foundKeys);
            return;
        }
        if(!propertyKeys.containsAll(foundKeys)){
            throw new IllegalArgumentException("Unexpected schema properties found on a page that do not match schema on first page");
        }else if(!foundKeys.containsAll(propertyKeys)){
            throw new IllegalArgumentException("Missing schema properties on page that were present in first page schema.");
        }
    }

    protected List<String> getRow(List<String> propertyKeys, Map<String, Object> object) {
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
}
