package de.hska.ld.core.config.security.openidconnect;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import org.mitre.jose.keystore.JWKSetKeyStore;
import org.mitre.jwt.signer.service.impl.DefaultJwtSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.JWKSetCacheService;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.keypublisher.ClientKeyPublisher;
import org.mitre.openid.connect.client.service.impl.*;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

@Component
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class ODICCoreConfig {

    public static final String OPENID_CONNECT_SERVER_WEBAPP = "https://api.learning-layers.eu/"; //"http://localhost:8080/openid-connect-server-webapp/";
    public static final String SIMPLE_WEB_APP_OPENID_CONNECT_LOGIN = "http://localhost:9000/simple-web-app/openid_connect_login";
    public static final String SIMPLE_WEB_APP_JWK = "http://localhost:9000/simple-web-app/jwk";
    public static final String ACCOUNT_CHOOSER = "http://localhost/account-chooser/";
    public static final String CLIENT_ID = "fad42d7c-0ae5-41c7-98ef-10ab37f9d2e8";
    public static final String CLIENT_SECRET = "secret";

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public StaticServerConfigurationService staticServerConfigurationService() {
        StaticServerConfigurationService staticServerConfigurationService = new StaticServerConfigurationService();
        Map<String, ServerConfiguration> serverConfigs = new HashMap<>();
        ServerConfiguration serverConfig = new ServerConfiguration();
        serverConfig.setIssuer(OPENID_CONNECT_SERVER_WEBAPP);
        serverConfig.setAuthorizationEndpointUri(OPENID_CONNECT_SERVER_WEBAPP + "authorize");
        serverConfig.setTokenEndpointUri(OPENID_CONNECT_SERVER_WEBAPP + "token");
        serverConfig.setUserInfoUri(OPENID_CONNECT_SERVER_WEBAPP + "userinfo");
        serverConfig.setJwksUri(OPENID_CONNECT_SERVER_WEBAPP + "jwk");
        serverConfigs.put(OPENID_CONNECT_SERVER_WEBAPP, serverConfig);
        staticServerConfigurationService.setServers(serverConfigs);
        return staticServerConfigurationService;
    }

    @Bean
    public HybridServerConfigurationService hybridServerConfigurationService() {
        HybridServerConfigurationService hybridServerConfigurationService = new HybridServerConfigurationService();
        Map<String, ServerConfiguration> serverConfigs = new HashMap<>();
        ServerConfiguration serverConfig = new ServerConfiguration();
        serverConfig.setIssuer(OPENID_CONNECT_SERVER_WEBAPP);
        serverConfig.setAuthorizationEndpointUri(OPENID_CONNECT_SERVER_WEBAPP + "authorize");
        serverConfig.setTokenEndpointUri(OPENID_CONNECT_SERVER_WEBAPP + "token");
        serverConfig.setUserInfoUri(OPENID_CONNECT_SERVER_WEBAPP + "userinfo");
        serverConfig.setJwksUri(OPENID_CONNECT_SERVER_WEBAPP + "jwk");
        serverConfigs.put(OPENID_CONNECT_SERVER_WEBAPP, serverConfig);
        hybridServerConfigurationService.setServers(serverConfigs);
        return hybridServerConfigurationService;
    }

    @Bean
    public HybridIssuerService hybridIssuerService() {
        HybridIssuerService hybridIssuerService = new HybridIssuerService();
        hybridIssuerService.setLoginPageUrl("login");
        return hybridIssuerService;
    }

    @Bean
    public DynamicServerConfigurationService dynamicServerConfigurationService() {
        return new DynamicServerConfigurationService();
    }

    @Bean
    public DynamicRegistrationClientConfigurationService dynamicClientConfigurationService() {
        DynamicRegistrationClientConfigurationService dynamicRegistrationClientConfigurationService = new DynamicRegistrationClientConfigurationService();

        Set<String> scope = new HashSet<>();
        scope.add("openid");
        scope.add("email");
        scope.add("address");
        scope.add("profile");
        scope.add("phone");

        Set<String> redirectUris = new HashSet<>();
        redirectUris.add(SIMPLE_WEB_APP_OPENID_CONNECT_LOGIN);

        RegisteredClient client = new RegisteredClient();
        client.setClientName("Simple Web App");
        client.setScope(scope);
        client.setTokenEndpointAuthMethod(ClientDetailsEntity.AuthMethod.SECRET_BASIC);
        client.setRedirectUris(redirectUris);
        client.setRequestObjectSigningAlg(JWSAlgorithm.RS256);
        client.setJwksUri(SIMPLE_WEB_APP_JWK);

        dynamicRegistrationClientConfigurationService.setTemplate(client);

        return dynamicRegistrationClientConfigurationService;
    }

    @Bean
    public StaticClientConfigurationService staticClientConfigurationService() {
        StaticClientConfigurationService staticClientConfigurationService = new StaticClientConfigurationService();

        Map<String, RegisteredClient> clientMap = new HashMap<>();

        Set<String> scope = new HashSet<>();
        scope.add("openid");
        scope.add("email");
        scope.add("address");
        scope.add("profile");
        scope.add("phone");

        Set<String> redirectUris = new HashSet<>();
        redirectUris.add(SIMPLE_WEB_APP_OPENID_CONNECT_LOGIN);

        RegisteredClient client = new RegisteredClient();
        client.setClientId(CLIENT_ID);
        client.setClientSecret(CLIENT_SECRET);
        client.setScope(scope);
        client.setTokenEndpointAuthMethod(ClientDetailsEntity.AuthMethod.SECRET_BASIC);
        client.setRedirectUris(redirectUris);

        clientMap.put(OPENID_CONNECT_SERVER_WEBAPP, client);

        staticClientConfigurationService.setClients(clientMap);

        return staticClientConfigurationService;
    }

    @Bean
    public HybridClientConfigurationService hybridClientConfigurationService() {
        HybridClientConfigurationService hybridClientConfigurationService = new HybridClientConfigurationService();

        Map<String, RegisteredClient> clientMap = new IdentityHashMap<>();

        Set<String> scope = new HashSet<>();
        scope.add("openid");
        scope.add("email");
        scope.add("address");
        scope.add("profile");
        scope.add("phone");

        Set<String> redirectUris = new HashSet<>();
        redirectUris.add(SIMPLE_WEB_APP_OPENID_CONNECT_LOGIN);

        RegisteredClient client = new RegisteredClient();
        client.setClientId(CLIENT_ID);
        client.setClientSecret(CLIENT_SECRET);
        client.setScope(scope);
        client.setTokenEndpointAuthMethod(ClientDetailsEntity.AuthMethod.SECRET_BASIC);
        client.setRedirectUris(redirectUris);

        clientMap.put(OPENID_CONNECT_SERVER_WEBAPP, client);

        hybridClientConfigurationService.setClients(clientMap);

        Set<String> scope2 = new HashSet<>();
        scope2.add("openid");
        scope2.add("email");
        scope2.add("address");
        scope2.add("profile");
        scope2.add("phone");

        Set<String> redirectUris2 = new HashSet<>();
        redirectUris2.add(SIMPLE_WEB_APP_OPENID_CONNECT_LOGIN);

        RegisteredClient client2 = new RegisteredClient();
        client2.setClientName("Simple Web App");
        client2.setScope(scope2);
        client2.setTokenEndpointAuthMethod(ClientDetailsEntity.AuthMethod.SECRET_BASIC);
        client2.setRedirectUris(redirectUris2);

        hybridClientConfigurationService.setTemplate(client2);

        return hybridClientConfigurationService;
    }

    @Bean
    public StaticAuthRequestOptionsService staticAuthRequestOptionsService() {
        // Enter options here...
        //HashMap map = new HashMap<>();
        //staticAuthRequestOptionsService.setOptions(map);
        return new StaticAuthRequestOptionsService();
    }

    @Bean
    public PlainAuthRequestUrlBuilder plainAuthRequestUrlBuilder() {
        return new PlainAuthRequestUrlBuilder();
    }

    @Bean
    public StaticSingleIssuerService staticIssuerService() {
        StaticSingleIssuerService staticSingleIssuerService = new StaticSingleIssuerService();
        staticSingleIssuerService.setIssuer(OPENID_CONNECT_SERVER_WEBAPP);
        return staticSingleIssuerService;
    }

    @Bean
    public WebfingerIssuerService webfingerIssuerService() {
        WebfingerIssuerService webfingerIssuerService = new WebfingerIssuerService();
        webfingerIssuerService.setLoginPageUrl("login");
        return webfingerIssuerService;
    }

    @Bean
    public ThirdPartyIssuerService thirdPartyIssuerService() {
        ThirdPartyIssuerService thirdPartyIssuerService = new ThirdPartyIssuerService();
        thirdPartyIssuerService.setAccountChooserUrl(ACCOUNT_CHOOSER);
        return thirdPartyIssuerService;
    }

    @Bean
    SignedAuthRequestUrlBuilder signedAuthRequestUrlBuilder() throws InvalidKeySpecException, NoSuchAlgorithmException {
        SignedAuthRequestUrlBuilder signedAuthRequestUrlBuilder = new SignedAuthRequestUrlBuilder();
        signedAuthRequestUrlBuilder.setSigningAndValidationService(defaultSignerService());
        return signedAuthRequestUrlBuilder;
    }

    @Bean
    public EncryptedAuthRequestUrlBuilder encryptedAuthRequestUrlBuilder() {
        EncryptedAuthRequestUrlBuilder encryptedAuthRequestUrlBuilder = new EncryptedAuthRequestUrlBuilder();

        JWKSetCacheService validatorCache = new JWKSetCacheService();

        encryptedAuthRequestUrlBuilder.setEncrypterService(validatorCache);

        encryptedAuthRequestUrlBuilder.setAlg(JWEAlgorithm.RSA1_5);
        encryptedAuthRequestUrlBuilder.setEnc(EncryptionMethod.A128GCM);

        return encryptedAuthRequestUrlBuilder;
    }

    @Bean
    public DefaultJwtSigningAndValidationService defaultSignerService() throws InvalidKeySpecException, NoSuchAlgorithmException {
        JWKSetKeyStore jWKSetKeyStore = new JWKSetKeyStore();
        Resource resource = new InputStreamResource(ODICCoreConfig.class.getResourceAsStream("/test.jwks"));
        //Resource resource = applicationContext.getResource("classpath:keystore.jwks");
        jWKSetKeyStore.setLocation(resource);

        DefaultJwtSigningAndValidationService defaultJwtSigningAndValidationService = new DefaultJwtSigningAndValidationService(jWKSetKeyStore);
        defaultJwtSigningAndValidationService.setDefaultSignerKeyId("rsa1");
        defaultJwtSigningAndValidationService.setDefaultSigningAlgorithmName("RS256");
        return defaultJwtSigningAndValidationService;
    }

    @Bean
    public ClientKeyPublisher clientKeyPublisher() throws InvalidKeySpecException, NoSuchAlgorithmException {
        ClientKeyPublisher clientKeyPublisher = new ClientKeyPublisher();
        clientKeyPublisher.setJwkPublishUrl("jwk");
        clientKeyPublisher.setSigningAndValidationService(defaultSignerService());
        return clientKeyPublisher;
    }
}
