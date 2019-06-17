package org.ga4gh.dataset.cli;

import org.everit.json.schema.Schema;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Column implements Comparable<Column> {

    private String name;
    private Schema schema;

    @Override
    public int compareTo(Column o) {
        Integer thisPosition =
                (Integer) this.schema.getUnprocessedProperties().get("x-ga4gh-position");
        Integer otherPosition =
                (Integer) o.schema.getUnprocessedProperties().get("x-ga4gh-position");
        if (thisPosition == null) {
            if (otherPosition == null) {
                return this.name.compareTo(o.name);
            } else {
                return 1;
            }
        } else {
            if (otherPosition == null) {
                return -1;
            } else {
                return thisPosition.compareTo(otherPosition);
            }
        }
    }
}
