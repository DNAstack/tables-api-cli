package com.dnastack.ga4gh.tables.cli.output.publish;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Pagination;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;

public interface Publisher {

    void publish(Table table);

    void publish(ListTableResponse table);

    void publish(TableData tableData, int pageNum);

    Boolean isBucketEmpty();

    String getObjectRoot(String destination);

    Pagination getAbsolutePagination(Pagination oldPagination, int pageNum);

    String toString(Object dataset);

}
