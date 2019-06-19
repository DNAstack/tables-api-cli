package org.ga4gh.dataset.cli.cmd;

import static java.util.Comparator.comparing;

import org.ga4gh.dataset.DatasetInfo;
import org.ga4gh.dataset.cli.ClientUtil;
import org.ga4gh.dataset.cli.ConfigUtil;
import org.ga4gh.dataset.cli.LoggingOptions;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "list", description = "List datasets")
public class List implements Runnable {

    @Mixin private LoggingOptions loggingOptions;

    @Override
    public void run() {
        try {
            loggingOptions.setupLogging();
            var client = ClientUtil.createClient(ConfigUtil.getUserConfig());

            CWC_LongestLine cwc = new CWC_LongestLine();
            cwc.add(4, 100);
            var datasets = client.listDatsets();
            AsciiTable at = new AsciiTable();
            at.getContext().setWidth(120);
            at.getRenderer().setCWC(cwc);
            at.addRule();
            at.addRow("Dataset ID", "Dataset description", "Schema").setPaddingLeftRight(1);
            at.addRule();
            datasets.sort(comparing(DatasetInfo::getId));
            for (var info : datasets) {
                at.addRow(info.getId(), info.getDescription(), info.getSchemaId())
                        .setPaddingLeftRight(1);
            }
            at.addRule();
            System.out.println(at.render());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
