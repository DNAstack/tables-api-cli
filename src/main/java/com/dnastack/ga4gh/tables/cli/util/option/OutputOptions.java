package com.dnastack.ga4gh.tables.cli.util.option;

import com.dnastack.ga4gh.tables.cli.output.FormattedOutputWriter;
import com.dnastack.ga4gh.tables.cli.output.OutputWriter;
import com.dnastack.ga4gh.tables.cli.output.PublishingOutputWriter;
import lombok.Getter;
import picocli.CommandLine.ArgGroup;

@Getter
public class OutputOptions {


    @ArgGroup(heading = "Output options")
    public ExclusiveOutputOptions outputOptions;

    @Getter
    static class ExclusiveOutputOptions {

        @ArgGroup(exclusive = false, heading = "Publish options")
        PublishOptions publishOptions;

        @ArgGroup(exclusive = false, heading = "Formatting options")
        OutputFormatOptions formatOptions;
    }


    public OutputWriter getWriter() {

        if (outputOptions == null) {
            outputOptions = new ExclusiveOutputOptions();
        }

        ExclusiveOutputOptions exclusiveOutputOptions = getOutputOptions();
        if (exclusiveOutputOptions.getPublishOptions() != null) {
            PublishOptions publishOptions = exclusiveOutputOptions.getPublishOptions();
            return new PublishingOutputWriter(publishOptions);
        } else {
            OutputFormatOptions outputFormatOptions = exclusiveOutputOptions.getFormatOptions();
            if (outputFormatOptions == null) {
                outputFormatOptions = new OutputFormatOptions();
            }
            return new FormattedOutputWriter(outputFormatOptions.getOutputMode(), System.out);
        }
    }

}
