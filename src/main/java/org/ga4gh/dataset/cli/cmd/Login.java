package org.ga4gh.dataset.cli.cmd;

import com.dnastack.ddap.cli.client.dam.DamInfo;
import com.dnastack.ddap.cli.client.dam.DdapFrontendClient;
import com.dnastack.ddap.cli.client.dam.LoginTokenResponse;
import com.dnastack.ddap.cli.login.BasicCredentials;
import com.dnastack.ddap.cli.login.Context;
import com.dnastack.ddap.cli.login.ContextDAO;
import com.dnastack.ddap.cli.login.LoginCommand;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.okhttp.OkHttpClient;
import org.ga4gh.dataset.cli.AuthOptions;
import org.ga4gh.dataset.cli.LoggingOptions;
import org.ga4gh.dataset.cli.OutputOptions;
import org.ga4gh.dataset.cli.PublishOptions;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Command(name = "login", description = "Login (*=required argument)", requiredOptionMarker='*', sortOptions = false)
public class Login implements Runnable {

    //@Mixin private LoggingOptions loggingOptions;
//    @Mixin private OutputOptions outputOptions;
//    @Mixin private AuthOptions authOptions;
//    @Mixin private PublishOptions publishOptions;

    @Option(
            names = {"-l"},
            description = "DDAP Root URL",
            required = true)
    private String ddapRootUrl;

    @Option(
            names = {"-u"},
            description = "DDAP Root URL",
            required = true)
    private String basicAuthUsername;

    @Option(
            names = {"-p"},
            description = "DDAP Root URL",
            required = true)
    private String basicAuthPassword;

    @Option(
            names = {"-r"},
            description = "DDAP Root URL",
            required = false)
    private String realm;

    @Override
    public void run() {
        //loggingOptions.setupLogging();
//        authOptions.initAuth();
        final BasicCredentials basicCredentials = new BasicCredentials(basicAuthUsername, basicAuthPassword);
        final ObjectMapper jsonMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        final ContextDAO contextDAO = new ContextDAO(new File(System.getenv("HOME")), jsonMapper);
        final DdapFrontendClient ddapFrontendClient = buildFeignClient(ddapRootUrl,
                basicCredentials,
                jsonMapper,
                false);

        final String realm = this.realm != null ? this.realm : "dnastack";
        final LoginCommand loginCommand = new LoginCommand(jsonMapper, ddapFrontendClient, realm);
        try {
            final LoginTokenResponse loginTokenResponse = loginCommand.login();
            final Map<String, DamInfo> damInfos = ddapFrontendClient.getDamInfos();
            contextDAO.persist(new Context(ddapRootUrl, loginCommand.getRealm(), damInfos, loginTokenResponse, basicCredentials));
            System.out.println("Login context saved");
        } catch (LoginCommand.LoginException | ContextDAO.PersistenceException e) {
            System.err.println(e.getMessage());
            //throw new CommandLineClient.SystemExit(1, e);
            System.exit(1);
        }
        //throw new CommandLineClient.SystemExit(0);
        System.exit(0);
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
