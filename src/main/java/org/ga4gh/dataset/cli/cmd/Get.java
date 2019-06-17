package org.ga4gh.dataset.cli.cmd;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;
import org.ga4gh.dataset.cli.ClientUtil;
import org.ga4gh.dataset.cli.Column;
import org.ga4gh.dataset.cli.ConfigUtil;

import com.fasterxml.jackson.databind.node.ObjectNode;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "get", description = "Get dataset")
public class Get implements Runnable {

    @Option(
            names = {"-I", "--dataset-id", "--id"},
            description = "Dataset ID",
            required = true)
    private String datasetId;

    @Override
    public void run() {
        try (var client = ClientUtil.createClient(ConfigUtil.getUserConfig())) {

            CWC_LongestLine cwc = new CWC_LongestLine();
            cwc.add(4, 100);
            var dataset = client.getDataset(datasetId);

            ObjectSchema schema = dataset.schema();
            Map<String, Schema> properties = schema.getPropertySchemas();
            var columns =
                    properties.entrySet().stream()
                            .map(entry -> new Column(entry.getKey(), entry.getValue()))
                            .collect(toList());
            columns.sort(Comparator.naturalOrder());
            AsciiTable at = new AsciiTable();
            at.getContext().setWidth(120);
            at.getRenderer().setCWC(cwc);
            at.addRule();
            var columnNames = columns.stream().map(Column::getName).collect(toList());
            at.addRow(columnNames).setPaddingLeftRight(1);
            at.addRule();
            for (var object : dataset) {
                at.addRow(getRow(columnNames, object)).setPaddingLeftRight(1);
            }
            at.addRule();
            System.out.println(at.render());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getRow(List<String> columns, ObjectNode node) {
        List<String> values = new ArrayList<>();
        for (var colname : columns) {
            String value = node.has(colname) ? node.get(colname).asText() : "";
            values.add(value);
        }
        return values;
    }
}
