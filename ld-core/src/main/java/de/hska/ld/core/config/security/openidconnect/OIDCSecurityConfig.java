package de.hska.ld.core.config.security.openidconnect;

import de.hska.ld.core.persistence.domain.Role;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.RoleService;
import de.hska.ld.core.service.UserService;
import org.mitre.oauth2.service.impl.DefaultClientUserDetailsService;
import org.mitre.openid.connect.client.*;
import org.mitre.openid.connect.client.service.impl.*;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.mitre.openid.connect.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        OIDCAuthenticationFilter oidcFilter = openIdConnectAuthenticationFilter();
        oidcFilter.setApplicationEventPublisher(new ApplicationEventPublisher() {
            @Override
            public void publishEvent(ApplicationEvent event) {
                System.out.println(event);
                Object source = event.getSource();
                if (source != null) {
                    OIDCAuthenticationToken token = (OIDCAuthenticationToken) source;
                    Map<String, String> map = (Map) token.getPrincipal();
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
                            if (!ODICCoreConfig.OPENID_CONNECT_SERVER_WEBAPP.equals(issuer)) {
                                throw new UnsupportedOperationException("Wrong or no issuer found!");
                            }
                        }
                    }

                    // TODO 1.0 check if sub is already stored in the cache (meaning that the info is already in the db)
                    // TODO in this case don't check for profile updates for (5 minutes)

                    // TODO 2.0 otherwise check if the info is in the db
                    User currentUserInDb = userService.findBySubIdAndIssuer(subId, issuer);
                    UserInfo oidcUserInfo = ((OIDCAuthenticationToken) source).getUserInfo();

                    if (currentUserInDb == null && oidcUserInfo != null) {
                        // create a new user
                        User user = new User();
                        // check for colliding user names (via preferred user name)
                        User userWithGivenPreferredUserName = userService.findByUsername(oidcUserInfo.getPreferredUsername());
                        int i = 0;
                        if (userWithGivenPreferredUserName != null) {
                            while (userWithGivenPreferredUserName != null) {
                                String prefferedUsername = oidcUserInfo.getPreferredUsername() + "#" + i;
                                userWithGivenPreferredUserName = userService.findByUsername(prefferedUsername);
                            }
                        } else {
                            user.setUsername(oidcUserInfo.getPreferredUsername());
                        }

                        user.setFullName(oidcUserInfo.getName());
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
                        user.setPassword(UUID.randomUUID().toString());
                        user.setSubId(subId);
                        user.setIssuer(issuer);
                        userService.save(user);
                        // update security context
                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
                        final SubjectIssuerGrantedAuthority[] oidcAuthority = new SubjectIssuerGrantedAuthority[1];
                        authorities.forEach(authority -> {
                            if (authority instanceof SubjectIssuerGrantedAuthority) {
                                oidcAuthority[0] = (SubjectIssuerGrantedAuthority) authority;
                            }
                        });

                        ArrayList<GrantedAuthority> newAuthorities = new ArrayList<GrantedAuthority>();
                        newAuthorities.add(oidcAuthority[0]);
                        user.getRoleList().forEach(role -> {
                            newAuthorities.add(new SimpleGrantedAuthority(role.getName()));
                        });
                        try {
                            Field authoritiesField = AbstractAuthenticationToken.class.getDeclaredField("authorities");
                            authoritiesField.setAccessible(true);
                            authoritiesField.set(auth, newAuthorities);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } else {
                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
                        final SubjectIssuerGrantedAuthority[] oidcAuthority = new SubjectIssuerGrantedAuthority[1];
                        authorities.forEach(authority -> {
                            if (authority instanceof SubjectIssuerGrantedAuthority) {
                                oidcAuthority[0] = (SubjectIssuerGrantedAuthority) authority;
                            }
                        });

                        ArrayList<GrantedAuthority> newAuthorities = new ArrayList<GrantedAuthority>();
                        newAuthorities.add(oidcAuthority[0]);
                        currentUserInDb.getRoleList().forEach(role -> {
                            newAuthorities.add(new SimpleGrantedAuthority(role.getName()));
                        });

                        try {
                            Field authoritiesField = AbstractAuthenticationToken.class.getDeclaredField("authorities");
                            authoritiesField.setAccessible(true);
                            authoritiesField.set(auth, newAuthorities);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                    // TODO in this case check for profile updates
                    // TODO check via equals if the user data is still up to date
                    // TODO 3.0 if the info is not in the db then create a new user in the db
                }
            }

            private Role createNewUserRole(String newRoleName) {
                Role newUserRole = new Role();
                newUserRole.setName(newRoleName);
                return roleService.save(newUserRole);
            }
        });

        // TODO add updated user info check

        http
                //.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint())
                //.and()
                .addFilterBefore(oidcFilter, AbstractPreAuthenticatedProcessingFilter.class)
                        //.csrf().disable()
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
                .logout()
                .deleteCookies("JSESSIONID");
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
        /*List<User> admins = userService.findAll(); //.findByRole("Admin");
        System.out.println(admins.get(0));*/

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

    /*@Bean
    public GenericFilterBean afterLoginFilter() throws Exception {
        GenericFilterBean filter = new GenericFilterBean() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                SecurityContext securityContext = SecurityContextHolder.getContext();
                Authentication authentication = securityContext.getAuthentication();
                if (authentication != null) {
                    Object principalObj = authentication.getPrincipal();
                    if (principalObj != null) {
                        String currentUser = (String) principalObj;
                        System.out.println(currentUser);
                    }
                }
            }

            @Override
            public void destroy() {

            }
        };
        return filter;
    }*/

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
