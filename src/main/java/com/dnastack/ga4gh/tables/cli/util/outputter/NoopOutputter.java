package com.dnastack.ga4gh.tables.cli.util.outputter;

import com.dnastack.ga4gh.tables.cli.model.ListTableResponse;
import com.dnastack.ga4gh.tables.cli.model.Table;
import com.dnastack.ga4gh.tables.cli.model.TableData;
import java.io.OutputStream;

//TODO: Dear code reader. Even as I write this I know I'm making a terrible, terrible mistake.
// Forgive me now for the sins you are about to witness.
public class NoopOutputter extends TableOutputter {

    public NoopOutputter(OutputStream stream) {
        super(stream);
    }

    @Override
    protected void outputHeader(TableData page) {
        return;
    }

    @Override
    protected void outputRows(TableData page) {
        return;
    }

    @Override
    protected void outputFooter(TableData page) {
        return;
    }

    @Override
    protected void outputInfo(Table table) {
        return;
    }

    @Override
    protected void outputTableList(ListTableResponse table) {
        return;
    }
}
