package com.dnastack.ga4gh.tables.cli.output;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import java.io.Closeable;

public interface OutputWriter extends Closeable {

    void write(Table table);

    void write(TableData tableData);

    void write(ListTableResponse tableResponse);

    void writeSearchResult(TableData tableData);
}
