package de.hska.ld.oidc.controller;

import de.hska.ld.content.dto.OIDCUserinfoDto;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.exception.ValidationException;
import de.hska.ld.core.persistence.domain.Role;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.RoleService;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import de.hska.ld.oidc.client.OIDCIdentityProviderClient;
import org.mitre.openid.connect.client.SubjectIssuerGrantedAuthority;
import org.mitre.openid.connect.model.DefaultUserInfo;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;

@RestController
@RequestMapping(Core.RESOURCE_USER + "/oidc")
public class OIDCController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @RequestMapping(method = RequestMethod.GET, value = "/authenticate")
    public Callable authenticate(@RequestParam String issuer, @RequestHeader String Authorization) {
        return () -> {
            return _authenticate(issuer, Authorization);
        };
    }

    private User _authenticate(String issuer, String Authorization) throws IOException {
        // 1. check that the OIDC token is set in the correct way
        String oidcToken = null;
        try {
            oidcToken = Authorization.substring("Bearer ".length(), Authorization.length());
        } catch (Exception e) {
            throw new ValidationException("malformed oidc token information");
        }

        // 2. check if the oidcUserinfo is accessible via the access token
        OIDCUserinfoDto oidcUserinfoDto = authenticateTowardsOIDCIdentityProvider(issuer, oidcToken);
        if (!oidcUserinfoDto.isEmailVerified()) {
            throw new ValidationException("user email not verified");
        }

        // 3. check if a a user account for this oidc user still exists within living documents
        User user = userService.findBySubIdAndIssuer(oidcUserinfoDto.getSub(), issuer + "/");
        if (user == null) {
            // 2.1. If the user does not already exist:
            //      Create the new user in the database
            user = creatNewUserFromOIDCUserinfo(issuer, oidcUserinfoDto, oidcToken);
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            enrichAuthoritiesWithStoredAuthorities(oidcUserinfoDto.getSub(), issuer + "/", oidcUserinfoDto, oidcToken, user, auth);
        }

        // 4.   After the user data has been retrieved or created:
        //      Update the security context with the needed information (give the user access to other rest resources)
        Authentication auth = null;
        try {
            auth = SecurityContextHolder.getContext().getAuthentication();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (auth == null) {
            throw new ValidationException("no security context for this user available");
        }

        return user;
    }

    /*@RequestMapping(method = RequestMethod.POST, value = "/token-auth")
    public Callable createDocument(@RequestBody Document document, @RequestParam String issuer, @RequestParam String accessToken, @RequestParam String discussionId) {
        return () -> {
            OIDCUserinfoDto userInfoDto = authenticateTowardsOIDCIdentityProvider(issuer, accessToken);

            // 2. Look in the database if the user is already known to the application
            // (via issuer and subject)
            User user = userService.findBySubIdAndIssuer(userInfoDto.getSub(), issuer + "/");
            if (user == null) {
                // 2.1 If the user is not yet known to the application create a new user in the database

                // create a new user
                user = new User();
                // check for colliding user names (via preferred user name)
                User userWithGivenPreferredUserName = userService.findByUsername(userInfoDto.getPreferredUsername());
                int i = 0;
                if (userWithGivenPreferredUserName != null) {
                    while (userWithGivenPreferredUserName != null) {
                        String prefferedUsername = userInfoDto.getPreferredUsername() + "#" + i;
                        userWithGivenPreferredUserName = userService.findByUsername(prefferedUsername);
                    }
                } else {
                    user.setUsername(userInfoDto.getPreferredUsername());
                }

                user.setFullName(userInfoDto.getName());
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
                if ("development".equals(System.getenv("LDS_APP_INSTANCE"))) {
                    user.setPassword("pass");
                } else {
                    user.setPassword(UUID.randomUUID().toString());
                }
                user.setSubId(userInfoDto.getSub());
                user.setIssuer(issuer + "/");
                String oidcUpdatedTime = userInfoDto.getUpdatedTime();
                // oidc time: "20150701_090039"
                // oidc format: "yyyyMMdd_HHmmss"
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                try {
                    Date date = sdf.parse(oidcUpdatedTime);
                    user.setLastupdatedAt(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                user = userService.save(user);
                // update security context
                // TODO set other attributes in SecurityContext
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                enrichAuthoritiesWithStoredAuthorities(user, auth);
            }

            // 3. Create the document in the database
            Document newDocument = documentService.save(document);

            // 4. Create the document in the SSS together with the link to the discussion
            // 4.1 Authenticate with the SSS
            // SSS auth Endpoint: http://test-ll.know-center.tugraz.at/layers.test/auth/auth/
            SSSClient sssClient = new SSSClient();
            SSSAuthDto sssAuthDto = sssClient.authenticate(accessToken);
            String sssUserURI = sssAuthDto.getUser();

            /*{
                "op": "authCheckCred",
                "user": "http://sss.eu/111977715382594029",
                "key": "eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjE0NDE3MTczMDYsImF1ZCI6WyJlZmQ1OTA0ZC0xOGZiLTRjNjUtYjFkZS1lZGExM2VlZjk1NjQiXSwiaXNzIjoiaHR0cHM6XC9cL2FwaS5sZWFybmluZy1sYXllcnMuZXVcL29cL29hdXRoMlwvIiwianRpIjoiZjdkYmU1ODEtZjg4OC00YTc4LTk0YjUtNTViMWM4NzViZjUyIiwiaWF0IjoxNDQxNzEzNzA2fQ.0pOUMkVfD62QqzaAW1BKC4pFg4LQ1Em7Y3OERfduHRzQtoUfoJ80hLDcprhmYT55I3rKhPdaOCMM62WtEHdi1iQIw3VyjXc6TAeVZkaXE07Nn4NM4uQyaixbc5V-dNDRpOmocCvpHGyrN2NMSr6J0-jnZuV2AwIuoqPk5_qm8IE"
            }*/

    // SSS livingdocs Endpoint: http://test-ll.know-center.tugraz.at/layers.test/livingdocs/livingdocs/
    //sssClient.getAllLDocs(accessToken);

            /*{
                "uri": "SSUri",
                "label": "SSLabel",
                "description": "SSTextComment",
                "discussion": "SSUri"
            }*/

    // 5. Send back the document
    //return new ResponseEntity<>(newDocument, HttpStatus.OK);
    //Document newDocument = documentService.save(document);
    //return new ResponseEntity<>(newDocument, HttpStatus.CREATED);
        /*};
    }*/

    private OIDCUserinfoDto authenticateTowardsOIDCIdentityProvider(String issuer, String oidcToken) throws ValidationException, IOException {
        // Retrieve oidc subject information from oidc identity provider
        // @ https://<oidc_endpoint>/userinfo?access_token=<accessToken>
        String[] allowedIssuers = new String[1];
        allowedIssuers[0] = "https://api.learning-layers.eu/o/oauth2";
        boolean issuerAllowed = false;
        for (String allowedIssuer : allowedIssuers) {
            if (allowedIssuer.equals(issuer)) {
                issuerAllowed = true;
            }
        }
        OIDCIdentityProviderClient client = new OIDCIdentityProviderClient();
        if (issuerAllowed) {
            return client.getUserinfo(issuer, oidcToken);
        } else {
            throw new ValidationException("issuer");
        }
    }

    @Transactional(readOnly = false)
    private User creatNewUserFromOIDCUserinfo(String issuer, OIDCUserinfoDto userInfoDto, String oidcToken) {
        // create a new user
        User user = new User();
        user.setEmail(userInfoDto.getEmail());
        // check for colliding user names (via preferred user name)
        User userWithGivenPreferredUserName = userService.findByUsername(userInfoDto.getPreferredUsername());
        int i = 0;
        if (userWithGivenPreferredUserName != null) {
            while (userWithGivenPreferredUserName != null) {
                String prefferedUsername = userInfoDto.getPreferredUsername() + "#" + i;
                userWithGivenPreferredUserName = userService.findByUsername(prefferedUsername);
            }
        } else {
            user.setUsername(userInfoDto.getPreferredUsername());
        }

        user.setFullName(userInfoDto.getName());
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
        if ("development".equals(System.getenv("LDS_APP_INSTANCE"))) {
            user.setPassword("pass");
        } else {
            user.setPassword(UUID.randomUUID().toString());
        }
        user.setSubId(userInfoDto.getSub());
        user.setIssuer(issuer + "/");
        String oidcUpdatedTime = userInfoDto.getUpdatedTime();
        // oidc time: "20150701_090039"
        // oidc format: "yyyyMMdd_HHmmss"
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        try {
            Date date = sdf.parse(oidcUpdatedTime);
            user.setLastupdatedAt(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        user = userService.save(user);
        // update security context
        // TODO set other attributes in SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        enrichAuthoritiesWithStoredAuthorities(userInfoDto.getSub(), issuer + "/", userInfoDto, oidcToken, user, auth);

        return user;
    }

    private Role createNewUserRole(String newRoleName) {
        Role newUserRole = new Role();
        newUserRole.setName(newRoleName);
        return roleService.save(newUserRole);
    }

    private void enrichAuthoritiesWithStoredAuthorities(String sub, String issuer, OIDCUserinfoDto oidcUserinfoDto, String oidcToken, User currentUserInDb, Authentication auth) {
        DefaultUserInfo userInfo = new DefaultUserInfo();
        userInfo.setSub(oidcUserinfoDto.getSub());
        userInfo.setEmail(oidcUserinfoDto.getEmail());
        userInfo.setName(oidcUserinfoDto.getName());
        userInfo.setEmailVerified(true);
        userInfo.setFamilyName(oidcUserinfoDto.getFamilyName());
        userInfo.setGivenName(oidcUserinfoDto.getGivenName());
        userInfo.setPreferredUsername(oidcUserinfoDto.getPreferredUsername());
        userInfo.setUpdatedTime(oidcUserinfoDto.getUpdatedTime());
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
        if (oidcAuthority[0] == null) {
            newAuthorities.add(new SubjectIssuerGrantedAuthority(sub, issuer));
        }
        OIDCAuthenticationToken token = new OIDCAuthenticationToken(sub, issuer, userInfo, newAuthorities, null, oidcToken, null);

        // update the authority information in the security context
        SecurityContextHolder.getContext().setAuthentication(token);
    }
}
