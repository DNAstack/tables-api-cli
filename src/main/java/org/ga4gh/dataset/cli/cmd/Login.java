package org.ga4gh.dataset.cli.cmd;

import com.dnastack.ddap.cli.client.dam.DamInfo;
import com.dnastack.ddap.cli.client.dam.DdapFrontendClient;
import com.dnastack.ddap.cli.client.dam.LoginTokenResponse;
import com.dnastack.ddap.cli.client.dam.ViewAccessTokenResponse;
import com.dnastack.ddap.cli.login.BasicCredentials;
import com.dnastack.ddap.cli.login.Context;
import com.dnastack.ddap.cli.login.ContextDAO;
import com.dnastack.ddap.cli.login.LoginCommand;
import com.dnastack.ddap.cli.resources.GetAccessCommand;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.okhttp.OkHttpClient;
import org.ga4gh.dataset.cli.AuthOptions;
import org.ga4gh.dataset.cli.util.ContextUtil;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Command(name = "login", description = "Login (*=required argument)", requiredOptionMarker='*', sortOptions = false)
public class Login implements Runnable {

    //TODO: These should probably come from the user's stored config file
    private final String DEFAULT_DAM_ID = "1";
    private final String DEFAULT_REALM = "mb_demo";
    private final String DEFAULT_RESOURCE_ID = "ga4gh-search";
    private final String DEFAULT_VIEW_ID = "search";
    private final String DEFAULT_DDAP_ROOT_URL = "https://ddap-mssng.staging.mss.ng";

    //@Mixin private LoggingOptions loggingOptions;
//    @Mixin private OutputOptions outputOptions;
    @CommandLine.Mixin
    private AuthOptions authOptions;
//    @Mixin private PublishOptions publishOptions;

    @Option(
            names = {"-l"},
            description = "DDAP Root URL",
            required = false)
    private String ddapRootUrl;

    @Option(
            names = {"-u"},
            description = "DDAP username",
            required = true)
    private String basicAuthUsername;

    @Option(
            names = {"-p"},
            description = "DDAP password",
            required = true)
    private String basicAuthPassword;

    @Option(
            names = {"-realm"},
            description = "DDAP Root URL",
            required = false)
    private String realm;

    @Option(
            names = {"-resource"},
            description = "DDAP Resource",
            required = false)
    private String resourceId;

    @Option(
            names = {"-view"},
            description = "Resrouce View ",
            required = false)
    private String viewId;

    @Option(
            names = {"-damId"},
            description = "DAM ID",
            required = false)
    private String damId;

    @Override
    public void run() {
        //loggingOptions.setupLogging();
        authOptions.initAuth();
        if (performIdentityLogin()) {
            getSearchAccess();
        }
    }

    private boolean performIdentityLogin() {
        final String realm = this.realm != null ? this.realm : DEFAULT_REALM;
        final String ddapRootUrl = this.ddapRootUrl != null ? this.ddapRootUrl : DEFAULT_DDAP_ROOT_URL;
        final BasicCredentials basicCredentials = new BasicCredentials(basicAuthUsername, basicAuthPassword);
        final ObjectMapper jsonMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        final ContextDAO contextDAO = new ContextDAO(new File(System.getenv("HOME")), jsonMapper);
        final DdapFrontendClient ddapFrontendClient = buildFeignClient(ddapRootUrl,
                basicCredentials,
                jsonMapper,
                false);

        final LoginCommand loginCommand = new LoginCommand(jsonMapper, ddapFrontendClient, realm);
        try {
            final LoginTokenResponse loginTokenResponse = loginCommand.login();
            final Map<String, DamInfo> damInfos = ddapFrontendClient.getDamInfos();
            contextDAO.persist(new Context(ddapRootUrl, loginCommand.getRealm(), damInfos, loginTokenResponse, basicCredentials));
            System.out.println("Login context saved");
        } catch (LoginCommand.LoginException | ContextDAO.PersistenceException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    private void getSearchAccess() {
        final String damId = this.damId != null ? this.damId : DEFAULT_DAM_ID;
        final String resourceId = this.resourceId != null ? this.resourceId : DEFAULT_RESOURCE_ID;
        final String viewId = this.viewId != null ? this.viewId : DEFAULT_VIEW_ID;
        final ObjectMapper jsonMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        final ContextDAO contextDAO = new ContextDAO(new File(System.getenv("HOME")), jsonMapper);

        Context context = ContextUtil.loadContextOrExit(contextDAO);
        final DdapFrontendClient ddapFrontendClient = buildFeignClient(context.getUrl(),
                context.getBasicCredentials(),
                jsonMapper,
                false);

        final Login.IOConsumer<String> outputAction;
        final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        outputAction = response -> yamlMapper.writer().writeValue(new File(System.getenv("HOME") + "/.dataset-api-cli"), response);
        try {
            final String ttl = "1h";//commandLine.getOptionValue(CliOptions.TTL_OPT, "1h");
            final DamInfo damInfo = context.getDamInfos().get(damId);
            if (damInfo == null) {
                System.err.printf("Invalid damId [%s]\n", damId);
                System.exit(1);
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
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Unable to serialize response.");
            System.exit(1);
        }

        System.exit(0);
    }

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
