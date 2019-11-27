package com.dnastack.ga4gh.tables.cli.input;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import java.util.Iterator;

public interface TableFetcher {

    ListTableResponse list();

    Iterator<TableData> getData(String tableName);

    Iterator<TableData> search(String query);

    Table getInfo(String tableName);

}
