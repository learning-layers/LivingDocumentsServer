package de.hska.ld.core.config.security.openidconnect;

import org.mitre.oauth2.service.impl.DefaultClientUserDetailsService;
import org.mitre.openid.connect.client.*;
import org.mitre.openid.connect.client.service.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebMvcSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, proxyTargetClass = true)
public class OIDCSecurityConfig extends WebSecurityConfigurerAdapter {

    // client configuration wiki
    // @see https://github.com/mitreid-connect/OpenID-Connect-Java-Spring-Server/wiki/Client-configuration

    // converted from servlet-context.xml
    // @see https://github.com/mitreid-connect/simple-web-app/blob/master/src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml#L400

    // executed 'java -jar json-web-key-generator-0.1-SNAPSHOT-jar-with-dependencies.jar -t RSA -s 1024 -S -i rsa1' to generate keystore
    // @see https://github.com/mitreid-connect/OpenID-Connect-Java-Spring-Server/wiki/Key-generation
    private AuthenticationManager authenticationManager;

    @Autowired
    private HybridIssuerService hybridIssuerService;

    @Autowired
    private DynamicServerConfigurationService dynamicServerConfigurationService;

    @Autowired
    private DynamicRegistrationClientConfigurationService dynamicClientConfigurationService;

    @Autowired
    private StaticAuthRequestOptionsService staticAuthRequestOptionsService;

    @Autowired
    private PlainAuthRequestUrlBuilder plainAuthRequestUrlBuilder;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(openIdConnectAuthenticationFilter(), AbstractPreAuthenticatedProcessingFilter.class)
                .logout();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {

    }

    @Bean
    public LoginUrlAuthenticationEntryPoint authenticationEntryPoint() {
        LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint = new LoginUrlAuthenticationEntryPoint("/openid_connect_login");
        return loginUrlAuthenticationEntryPoint;
    }

    @Override
    public AuthenticationManager authenticationManager() throws Exception {
        List<AuthenticationProvider> authenticationProviderList = new ArrayList<>();
        OIDCAuthenticationProvider provider = authenticationProvider();
        authenticationProviderList.add(provider);
        authenticationManager = new ProviderManager(authenticationProviderList);
        return authenticationManager;
    }

    @Bean
    public OIDCAuthenticationProvider authenticationProvider() {
        OIDCAuthenticationProvider authenticationProvider = new OIDCAuthenticationProvider();
        NamedAdminAuthoritiesMapper namedAdminAuthoritiesMapper = new NamedAdminAuthoritiesMapper();
        namedAdminAuthoritiesMapper.setAdmins(namedAdmins());
        authenticationProvider.setAuthoritiesMapper(namedAdminAuthoritiesMapper);
        return authenticationProvider;
    }

    @Bean
    public ClientDetailsService clientDetailsService() {
        DefaultClientUserDetailsService userDetailsService = new DefaultClientUserDetailsService();
        ClientDetailsService clientDetailsService = userDetailsService.getClientDetailsService();
        return clientDetailsService;
    }

    @Bean
    public Set<SubjectIssuerGrantedAuthority> namedAdmins() {
        SubjectIssuerGrantedAuthority subjectIssuerGrantedAuthority = new SubjectIssuerGrantedAuthority("90342.ASDFJWFA",
                "https://mitreid.org/");
        Set<SubjectIssuerGrantedAuthority> subjectIssuerGrantedAuthorities = new HashSet<>();
        subjectIssuerGrantedAuthorities.add(subjectIssuerGrantedAuthority);
        return subjectIssuerGrantedAuthorities;
    }

    @Bean
    public OIDCAuthenticationFilter openIdConnectAuthenticationFilter() throws Exception {
        OIDCAuthenticationFilter oidcAuthenticationFilter = MitreOidcHelper.createOIDCAuthenticationFilter();
        oidcAuthenticationFilter.setAuthenticationManager(authenticationManager);

        oidcAuthenticationFilter.setIssuerService(hybridIssuerService);
        oidcAuthenticationFilter.setServerConfigurationService(dynamicServerConfigurationService);
        oidcAuthenticationFilter.setClientConfigurationService(dynamicClientConfigurationService);
        oidcAuthenticationFilter.setAuthRequestOptionsService(staticAuthRequestOptionsService);
        oidcAuthenticationFilter.setAuthRequestUrlBuilder(plainAuthRequestUrlBuilder);
        return oidcAuthenticationFilter;
    }
}
