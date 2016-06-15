/*
 *  Code contributed to the Learning Layers project
 *  http://www.learning-layers.eu
 *  Development is partly funded by the FP7 Programme of the European
 *  Commission under Grant Agreement FP7-ICT-318209.
 *  Copyright (c) 2015, Karlsruhe University of Applied Sciences.
 *  For a list of contributors see the AUTHORS file at the top-level directory
 *  of this distribution.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.hska.ld.core.config.security.openidconnect;

import de.hska.ld.core.config.security.AjaxAuthenticationFailureHandler;
import de.hska.ld.core.config.security.AjaxAuthenticationSuccessHandler;
import de.hska.ld.core.config.security.AjaxLogoutSuccessHandler;
import de.hska.ld.core.config.security.FormAuthenticationProvider;
import de.hska.ld.core.events.user.UserEventsPublisher;
import de.hska.ld.core.persistence.domain.Role;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.RoleService;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.EscapeUtil;
import org.mitre.oauth2.service.impl.DefaultClientUserDetailsService;
import org.mitre.openid.connect.client.*;
import org.mitre.openid.connect.client.service.impl.*;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.mitre.openid.connect.model.UserInfo;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.LoggingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

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

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserEventsPublisher userEventsPublisher;

    @Autowired
    private Environment env;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void configure(HttpSecurity http) throws Exception {
        OIDCAuthenticationFilter oidcFilter = openIdConnectAuthenticationFilter();
        oidcFilter.setAuthenticationSuccessHandler(new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                response.sendRedirect(env.getProperty("module.core.oidc.redirect.to.client"));
            }
        });
        oidcFilter.setApplicationEventPublisher(new ApplicationEventPublisher() {
            @Override
            public void publishEvent(ApplicationEvent event) {
                Object source = event.getSource();
                OIDCAuthenticationToken token = null;
                if (source != null) {
                    token = (OIDCAuthenticationToken) source;
                }
                if (token != null) {
                    Map map = (Map) token.getPrincipal();
                    Iterator iterator = map.entrySet().iterator();
                    String subId = null;
                    String issuer = null;
                    if (iterator.hasNext()) {
                        Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
                        if ("sub".equals(entry.getKey())) {
                            // check if sub id is already present in the database
                            subId = entry.getValue();
                            if (subId == null) {
                                throw new UnsupportedOperationException("No subId found!");
                            }
                        }
                    }
                    if (iterator.hasNext()) {
                        Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
                        if ("iss".equals(entry.getKey())) {
                            issuer = entry.getValue();
                            if (!env.getProperty("module.core.oidc.identity.provider.url").equals(issuer)) {
                                throw new UnsupportedOperationException("Wrong or no issuer found!");
                            }
                        }
                    }

                    User currentUserInDb = userService.findBySubIdAndIssuer(subId, issuer);
                    UserInfo oidcUserInfo = ((OIDCAuthenticationToken) source).getUserInfo();

                    if (currentUserInDb == null && oidcUserInfo != null) {
                        User savedUser = createNewUserFirstLogin(token, subId, issuer, oidcUserInfo);
                        try {
                            userEventsPublisher.sendUserLoginEvent(savedUser);
                            userEventsPublisher.sendUserFirstLoginEvent(savedUser);
                        } catch (Exception e) {
                            //
                        }
                        LoggingContext.put("user_email", EscapeUtil.escapeJsonForLogging(savedUser.getEmail()));
                        Logger.trace("User logs in for the first time.");
                        LoggingContext.clear();
                    } else if (oidcUserInfo != null) {
                        User savedUser = updateUserInformationFromOIDC(token, currentUserInDb, oidcUserInfo);
                        try {
                            userEventsPublisher.sendUserLoginEvent(savedUser);
                            userEventsPublisher.sendUserFirstLoginEvent(savedUser);
                        } catch (Exception e) {
                            //
                        }
                        LoggingContext.put("user_email", EscapeUtil.escapeJsonForLogging(savedUser.getEmail()));
                        Logger.trace("User logs in.");
                        LoggingContext.clear();
                    } else {
                        // oidc information is null
                        throw new UnsupportedOperationException("No OIDC information found!");
                    }
                }
            }

            private User updateUserInformationFromOIDC(OIDCAuthenticationToken token, User currentUserInDb, UserInfo oidcUserInfo) {
                // get the current authentication details of the user
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                enrichAuthoritiesWithStoredAuthorities(currentUserInDb, auth);

                // check for profile updates since the last login
                String oidcUpdatedTime = token.getUserInfo().getUpdatedTime();
                // oidc time: "20150701_090039"
                // oidc format: "yyyyMMdd_HHmmss"
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                User savedUser = null;
                try {
                    Date date = sdf.parse(oidcUpdatedTime);
                    if (currentUserInDb.getEmail() == null || currentUserInDb.getLastupdatedAt().getTime() > date.getTime()) {
                        currentUserInDb.setFullName(oidcUserInfo.getName());
                        currentUserInDb.setEmail(oidcUserInfo.getEmail());
                        savedUser = userService.save(currentUserInDb);
                    } else {
                        savedUser = currentUserInDb;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return savedUser;
            }

            private User createNewUserFirstLogin(OIDCAuthenticationToken token, String subId, String issuer, UserInfo oidcUserInfo) {
                // create a new user
                User user = new User();
                // check for colliding user names (via preferred user name)
                String prefferedUsername = oidcUserInfo.getPreferredUsername();
                User userWithGivenPreferredUserName = userService.findByUsername(prefferedUsername);
                int i = 0;
                if (userWithGivenPreferredUserName != null) {
                    while (userWithGivenPreferredUserName != null) {
                        prefferedUsername = oidcUserInfo.getPreferredUsername() + "#" + i;
                        userWithGivenPreferredUserName = userService.findByUsername(prefferedUsername);
                    }
                }
                user.setUsername(prefferedUsername);

                user.setFullName(oidcUserInfo.getName());
                user.setEmail(oidcUserInfo.getEmail());
                user.setEnabled(true);
                // apply roles
                List<Role> roleList = new ArrayList<Role>();
                Role userRole = roleService.findByName("ROLE_USER");
                if (userRole == null) {
                    // create initial roles
                    String newUserRoleName = "ROLE_USER";
                    userRole = createNewUserRole(newUserRoleName);
                    String newAdminRoleName = "ROLE_ADMIN";
                    Role adminRole = createNewUserRole(newAdminRoleName);
                    // For the first user add the admin role
                    roleList.add(adminRole);
                } else {
                    roleList.add(userRole);
                }
                user.setRoleList(roleList);
                // A password is required so we set a uuid generated one
                if ("development".equals(env.getProperty("lds.app.instance"))) {
                    user.setPassword("pass");
                } else {
                    user.setPassword(UUID.randomUUID().toString());
                }
                user.setSubId(subId);
                user.setIssuer(issuer);
                String oidcUpdatedTime = token.getUserInfo().getUpdatedTime();
                // oidc time: "20150701_090039"
                // oidc format: "yyyyMMdd_HHmmss"
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                try {
                    Date date = sdf.parse(oidcUpdatedTime);
                    user.setLastupdatedAt(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                User savedUser = userService.save(user);

                // update security context
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                enrichAuthoritiesWithStoredAuthorities(user, auth);

                return savedUser;
            }

            @Override
            public void publishEvent(Object event) {
                throw new RuntimeException("Publish event call failed not implemented yet.");
            }

            private void enrichAuthoritiesWithStoredAuthorities(User currentUserInDb, Authentication auth) {
                Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
                final SubjectIssuerGrantedAuthority[] oidcAuthority = new SubjectIssuerGrantedAuthority[1];
                authorities.forEach(authority -> {
                    if (authority instanceof SubjectIssuerGrantedAuthority) {
                        // extract the oidc authority information
                        oidcAuthority[0] = (SubjectIssuerGrantedAuthority) authority;
                    }
                });

                // create new authorities that includes the authorities stored in the database
                // as well as the oidc authority
                ArrayList<GrantedAuthority> newAuthorities = new ArrayList<GrantedAuthority>();
                newAuthorities.add(oidcAuthority[0]);
                currentUserInDb.getRoleList().forEach(role -> {
                    newAuthorities.add(new SimpleGrantedAuthority(role.getName()));
                });
                try {
                    Field authoritiesField = AbstractAuthenticationToken.class.getDeclaredField("authorities");
                    authoritiesField.setAccessible(true);
                    authoritiesField.set(auth, newAuthorities);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                // update the authority information in the security context
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            private Role createNewUserRole(String newRoleName) {
                Role newUserRole = new Role();
                newUserRole.setName(newRoleName);
                return roleService.save(newUserRole);
            }
        });

        http
                .addFilterBefore(oidcFilter, AbstractPreAuthenticatedProcessingFilter.class)
                .csrf().requireCsrfProtectionMatcher(new RequestMatcher() {
            private Pattern allowedMethods =
                    Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");

            private RegexRequestMatcher apiMatcher =
                    new RegexRequestMatcher("/v[0-9]*/.*", null);

            @Override
            public boolean matches(HttpServletRequest request) {
                // CSRF disabled on allowedMethod
                if (allowedMethods.matcher(request.getMethod()).matches())
                    return false;

                // CSRF disabled on api calls
                if (apiMatcher.matches(request))
                    return false;

                // CSRF enables for other requests
                //TODO change later on
                return false;
            }
        })
                .and()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint())
                .and()
                .logout()
                .logoutSuccessHandler(logoutSuccessHandler())
                .deleteCookies("JSESSIONID")
                .deleteCookies("sessionID");
    }

    @Bean
    public LoginUrlAuthenticationEntryPoint authenticationEntryPoint() {
        return new LoginUrlAuthenticationEntryPoint("/openid_connect_login");
    }

    @Override
    public AuthenticationManager authenticationManager() throws Exception {
        List<AuthenticationProvider> authenticationProviderList = new ArrayList<>();
        OIDCAuthenticationProvider provider = authenticationProvider();
        authenticationProviderList.add(provider);
        authenticationProviderList.add(formAuthenticationProvider());
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
    public FormAuthenticationProvider formAuthenticationProvider() {
        return new FormAuthenticationProvider();
    }

    @Bean
    public ClientDetailsService clientDetailsService() {
        DefaultClientUserDetailsService userDetailsService = new DefaultClientUserDetailsService();
        return userDetailsService.getClientDetailsService();
    }

    @Bean
    public Set<SubjectIssuerGrantedAuthority> namedAdmins() {
        // Remove this.
        // Instead the first user to log in via open id connect automatically receives the admin role.
        return new HashSet<>();
    }

    @Bean
    public OIDCAuthenticationFilter openIdConnectAuthenticationFilter() throws Exception {
        OIDCAuthenticationFilter oidcAuthenticationFilter = MitreOidcHelper.createOIDCAuthenticationFilter(env);
        oidcAuthenticationFilter.setAuthenticationManager(authenticationManager);

        oidcAuthenticationFilter.setIssuerService(hybridIssuerService);
        oidcAuthenticationFilter.setServerConfigurationService(dynamicServerConfigurationService);
        oidcAuthenticationFilter.setClientConfigurationService(dynamicClientConfigurationService);
        oidcAuthenticationFilter.setAuthRequestOptionsService(staticAuthRequestOptionsService);
        oidcAuthenticationFilter.setAuthRequestUrlBuilder(plainAuthRequestUrlBuilder);
        return oidcAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AjaxAuthenticationSuccessHandler();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new AjaxAuthenticationFailureHandler();
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new AjaxLogoutSuccessHandler();
    }
}
