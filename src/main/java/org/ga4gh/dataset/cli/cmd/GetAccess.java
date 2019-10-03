package org.ga4gh.dataset.cli.cmd;

import com.dnastack.ddap.cli.client.dam.DamInfo;
import com.dnastack.ddap.cli.client.dam.DdapFrontendClient;
import com.dnastack.ddap.cli.client.dam.ViewAccessTokenResponse;
import com.dnastack.ddap.cli.login.BasicCredentials;
import com.dnastack.ddap.cli.login.Context;
import com.dnastack.ddap.cli.login.ContextDAO;
import com.dnastack.ddap.cli.resources.GetAccessCommand;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.okhttp.OkHttpClient;
import org.ga4gh.dataset.cli.LoggingOptions;
import org.ga4gh.dataset.cli.util.ContextUtil;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

@Command(name = "access", description = "Get Access (*=required argument)", requiredOptionMarker='*', sortOptions = false)
public class GetAccess implements Runnable {

    @Mixin private LoggingOptions loggingOptions;
//    @Mixin private OutputOptions outputOptions;
//    @Mixin private AuthOptions authOptions;
//    @Mixin private PublishOptions publishOptions;

    @Option(
            names = {"-r"},
            description = "DDAP Resource",
            required = true)
    private String resourceId;

    @Option(
            names = {"-v"},
            description = "Resrouce View ",
            required = true)
    private String viewId;

    @Option(
            names = {"-i"},
            description = "DAM ID",
            required = true)
    private String damId;

    @Override
    public void run() {
        loggingOptions.setupLogging();
//        authOptions.initAuth();
        final ObjectMapper jsonMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        final ContextDAO contextDAO = new ContextDAO(new File(System.getenv("HOME")), jsonMapper);
        Context context = ContextUtil.loadContextOrExit(contextDAO);
        final DdapFrontendClient ddapFrontendClient = buildFeignClient(context.getUrl(),
                context.getBasicCredentials(),
                jsonMapper,
                false);

        //final IOConsumer<ViewAccessTokenResponse> outputAction;
        final IOConsumer<String> outputAction;
        final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
//        if (commandLine.hasOption(CliOptions.FILE_OPT)) {
//            final File outputFile = setupEnvFileOrExit(commandLine);
//            outputAction = response -> writeOutputToEnvFile(outputFile, response, System.out);
//        } else {
//            outputAction = response -> yamlMapper.writer().writeValue(System.out, response);
//        }
        //outputAction = response -> yamlMapper.writer().writeValue(System.out, response);
        outputAction = response -> yamlMapper.writer().writeValue(new File(System.getenv("HOME") + "/.dataset-api-cli"), response);
        try {
            //final String damId = commandLine.getOptionValue(CliOptions.DAM_ID_OPT);
            //final String resourceId = commandLine.getOptionValue(CliOptions.RESOURCE_OPT);
            //final String viewId = commandLine.getOptionValue(CliOptions.VIEW_OPT);
            final String ttl = "1h";//commandLine.getOptionValue(CliOptions.TTL_OPT, "1h");

            final DamInfo damInfo = context.getDamInfos().get(damId);
            if (damInfo == null) {
                System.err.printf("Invalid damId [%s]\n", damId);
                //throw new CommandLineClient.SystemExit(1);
                System.exit(0);
            }

            final ViewAccessTokenResponse response = new GetAccessCommand(context, ddapFrontendClient, jsonMapper)
                    .getAccessToken(damInfo,
                            resourceId,
                            viewId,
                            ttl);
            System.out.println("Access token acquired");
            outputAction.accept(response.getToken());
        } catch (GetAccessCommand.GetAccessException e) {
            System.out.println(e.getMessage());
            //throw new CommandLineClient.SystemExit(1, e);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Unable to serialize response.");
            //throw new CommandLineClient.SystemExit(1, e);
            System.exit(1);
        }

        System.exit(0);
        //throw new CommandLineClient.SystemExit(0);

    }
//
//    private static persistAccessToken() {
//
//    }

    //TODO: Scream, then don't do the below

    @FunctionalInterface
    private interface IOConsumer<T> {
        void accept(T t) throws IOException;
    }

    private static DdapFrontendClient buildFeignClient(String ddapRootUrl,
                                                       BasicCredentials basicCredentials,
                                                       ObjectMapper objectMapper,
                                                       boolean debugLogging) {
        final Optional<String> encodedCredentials = Optional.ofNullable(basicCredentials)
                .map(bc -> Base64.getEncoder()
                        .encodeToString((bc.getUsername() + ":" + bc
                                .getPassword()).getBytes()));
        final Feign.Builder builder = Feign.builder()
                .client(new OkHttpClient())
                .decoder(new JacksonDecoder(objectMapper))
                .logLevel(debugLogging ? Logger.Level.FULL : Logger.Level.NONE)
                .logger(new Logger() {
                    @Override
                    protected void log(String configKey, String format, Object... args) {
                        System.out.printf(configKey + " " + format + "\n", args);
                    }
                });

        return encodedCredentials
                .map(ec -> builder.requestInterceptor(template -> template.header("Authorization", "Basic " + ec)))
                .orElse(builder)
                .target(DdapFrontendClient.class, ddapRootUrl);
    }
}
