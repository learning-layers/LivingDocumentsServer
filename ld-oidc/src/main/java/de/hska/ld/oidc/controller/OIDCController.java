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

package de.hska.ld.oidc.controller;

import de.hska.ld.content.events.document.DocumentEventsPublisher;
import de.hska.ld.content.persistence.domain.Attachment;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.exception.NotFoundException;
import de.hska.ld.core.exception.UserNotAuthorizedException;
import de.hska.ld.core.exception.ValidationException;
import de.hska.ld.core.persistence.domain.Role;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.RoleService;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import de.hska.ld.oidc.client.OIDCIdentityProviderClient;
import de.hska.ld.oidc.client.SSSClient;
import de.hska.ld.oidc.client.exception.AuthenticationNotValidException;
import de.hska.ld.oidc.dto.OIDCSubInfoDto;
import de.hska.ld.oidc.dto.OIDCUserinfoDto;
import de.hska.ld.oidc.dto.SSSAuthDto;
import de.hska.ld.oidc.dto.SSSLivingdocsResponseDto;
import de.hska.ld.oidc.service.UserSharingBufferService;
import org.mitre.openid.connect.client.SubjectIssuerGrantedAuthority;
import org.mitre.openid.connect.model.DefaultUserInfo;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedClientException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping(Core.RESOURCE_USER + "/oidc")
public class OIDCController {

    @Autowired
    private SSSClient sssClient;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private Environment env;

    @Autowired
    private UserSharingBufferService userSharingBufferService;

    @Autowired
    private DocumentEventsPublisher documentEventsPublisher;

    @RequestMapping(method = RequestMethod.GET, value = "/authenticate")
    public User authenticate(HttpServletRequest request,
                             @RequestParam(defaultValue = "https://api.learning-layers.eu/o/oauth2") String issuer,
                             @RequestHeader String Authorization,
                             @RequestParam(defaultValue = "false") boolean forceUpdate) {
        try {
            return _authenticate(request, issuer, Authorization, forceUpdate);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ValidationException("login failed");
        }
    }

    private User _authenticate(HttpServletRequest request, String issuer, String Authorization) throws IOException {
        return this._authenticate(request, issuer, Authorization, false);
    }

    private User _authenticate(HttpServletRequest request, String issuer, String Authorization, boolean forceUpdate) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

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
            // 3.1. If the user does NOT already exist:
            //      Create the new user in the database
            user = creatNewUserFromOIDCUserinfo(request, issuer, oidcUserinfoDto, oidcToken);
        } else {
            // 3.2. If the user does already exist:
            //      update the user's authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            enrichAuthoritiesWithStoredAuthorities(request, oidcUserinfoDto.getSub(), issuer + "/", oidcUserinfoDto, oidcToken, user, auth);
        }

        if (!authentication.isAuthenticated() || forceUpdate) {
            if (request.getSession(false) == null) {
                request.getSession(true);
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
            } else {
                try {
                    Field detailsField = AbstractAuthenticationToken.class.getDeclaredField("details");
                    detailsField.setAccessible(true);
                    HttpSession session = request.getSession(false);
                /*if (session == null || !session.isNew()) {
                   //session = request.getSession(true);
                }*/
                    Field authenticatedField = AbstractAuthenticationToken.class.getDeclaredField("authenticated");
                    authenticatedField.setAccessible(true);
                    authenticatedField.set(auth, true);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return user;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/document/{documentId}/share/issuerandsub")
    @Transactional
    public ResponseEntity shareDocumentViaIssuerAndSub(HttpServletRequest request,
                                                       @RequestBody List<OIDCSubInfoDto> issuerSubList,
                                                       @PathVariable Long documentId,
                                                       @RequestParam(defaultValue = "https://api.learning-layers.eu/o/oauth2") String issuer,
                                                       @RequestHeader(required = false) String Authorization) throws IOException, ServletException {

        _authenticate(request, issuer, Authorization);

        Document document = documentService.findById(documentId);
        if (document == null) {
            throw new NotFoundException("documentId");
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        List<OIDCSubInfoDto> userIssuerAndSubsThatDontHaveALDAccountRightNow = new ArrayList<>();
        for (OIDCSubInfoDto isserSubDto : issuerSubList) {
            User user = userService.findBySubIdAndIssuer(isserSubDto.getSub(), isserSubDto.getIssuer());
            if (user != null && user.getId() != null && document.getCreator() != null &&
                    document.getCreator().getId() != null && !user.getId().equals(document.getCreator().getId())) {
                if (first) {
                    sb.append(user.getId());
                    first = false;
                } else {
                    sb.append(";").append(user.getId());
                }
            } else if (user == null) {
                // remember the issuer and subs used in the sharing process and store them in
                // case these users login into living documents at a later time
                userIssuerAndSubsThatDontHaveALDAccountRightNow.add(isserSubDto);
            }
        }

        try {
            // remember all the user sub and issuers that the document should be shared with but which are
            // not know to the system right now
            userIssuerAndSubsThatDontHaveALDAccountRightNow.forEach(isserSubDto -> {
                userSharingBufferService.addUserSharingBuffer(documentId, isserSubDto.getSub(), isserSubDto.getIssuer(), "READ;WRITE");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        String userIds = sb.toString();
        if (!"".equals(userIds)) {
            Document dbDocument = documentService.findById(document.getId());
            dbDocument = addAccessWithNewTransaction(dbDocument.getId(), userIds, "READ;WRITE");
            try {
                String oidcToken = null;
                try {
                    oidcToken = Authorization.substring("Bearer ".length(), Authorization.length());
                } catch (Exception e) {
                    throw new ValidationException("malformed oidc token information");
                }
                documentEventsPublisher.sendDocumentSharingEvent(dbDocument, oidcToken, dbDocument.getAccessList());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ResponseEntity<>(dbDocument.getAccessList(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/document/{documentId}/share/email")
    @Transactional
    public ResponseEntity shareDocumentViaEmailAdress(HttpServletRequest request,
                                                      @RequestBody List<String> userEmailList,
                                                      @PathVariable Long documentId,
                                                      @RequestParam(defaultValue = "https://api.learning-layers.eu/o/oauth2") String issuer,
                                                      @RequestHeader(required = false) String Authorization) throws IOException, ServletException {

        _authenticate(request, issuer, Authorization);

        Document document = documentService.findById(documentId);
        if (document == null) {
            throw new NotFoundException("documentId");
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        List<String> userEmailsThatDontHaveALDAccountRightNow = new ArrayList<>();
        for (String userEmail : userEmailList) {
            User user = userService.findByEmail(userEmail);
            if (user != null && user.getId() != null && document.getCreator() != null &&
                    document.getCreator().getId() != null && !user.getId().equals(document.getCreator().getId())) {
                if (first) {
                    sb.append(user.getId());
                    first = false;
                } else {
                    sb.append(";").append(user.getId());
                }
            } else if (user == null) {
                // remember the email address used in the sharing process and store them in
                // case these users login into living documents at a later time
                userEmailsThatDontHaveALDAccountRightNow.add(userEmail);
            }
        }

        // remember all the user emails that the document should be shared with but which are
        // not know to the system right now
        try {
            userEmailsThatDontHaveALDAccountRightNow.forEach(userEmail -> {
                userSharingBufferService.addUserSharingBuffer(documentId, userEmail, "READ;WRITE");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        String userIds = sb.toString();
        Document dbDocument = null;
        if (!"".equals(userIds)) {
            dbDocument = documentService.findById(document.getId());
            dbDocument = addAccessWithNewTransaction(dbDocument.getId(), userIds, "READ;WRITE");
            try {
                String oidcToken = null;
                try {
                    oidcToken = Authorization.substring("Bearer ".length(), Authorization.length());
                } catch (Exception e) {
                    throw new ValidationException("malformed oidc token information");
                }
                documentEventsPublisher.sendDocumentSharingEvent(dbDocument, oidcToken, dbDocument.getAccessList());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ResponseEntity<>(dbDocument.getAccessList(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Document addAccessWithNewTransaction(Long dbDocument, String userIds, String permissions) {
        return documentService.addAccessWithoutTransactional(dbDocument, userIds, permissions);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/document")
    @Transactional(readOnly = false, rollbackFor = RuntimeException.class)
    public Document createDocument(HttpServletRequest request,
                                   @RequestBody Document document,
                                   @RequestParam(defaultValue = "https://api.learning-layers.eu/o/oauth2") String issuer,
                                   @RequestHeader(required = false) String Authorization,
                                   @RequestParam(required = false) String discussionId) throws IOException, ServletException {

        _authenticate(request, issuer, Authorization);

        // 3. Create the document in the database
        Document newDocument = documentService.save(document);
        if (document.getDescription() != null) {
            Attachment mainAttachment = newDocument.getAttachmentList().get(0);
            mainAttachment.setSource(document.getDescription().getBytes());
            //document.setDescription("");
            documentService.save(newDocument);
        }

        // 4. Create the document in the SSS together with the link to the discussion
        // 4.1 Authenticate with the SSS
        // SSS auth Endpoint: http://test-ll.know-center.tugraz.at/layers.test/auth/auth/
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //if (auth instanceof AnonymousAuthenticationToken) {
        OIDCAuthenticationToken token = (OIDCAuthenticationToken) auth;
        SSSAuthDto sssAuthDto = null;
        try {
            sssAuthDto = sssClient.authenticate(token.getAccessTokenValue());
        } catch (UserNotAuthorizedException e) {
            request.logout();
            e.printStackTrace();
            throw new UnauthorizedClientException("oidc token invalid");
        }

        // 4.2 Create the according SSSLivingdocs entity
        try {
            SSSLivingdocsResponseDto sssLivingdocsResponseDto = sssClient.createDocument(document, discussionId, token.getAccessTokenValue());
        } catch (AuthenticationNotValidException eAuth) {
            throw new UserNotAuthorizedException();
        }

        // 4.3 Retrieve the list of email addresses that have access to the livingdocument in the SSS
        // TODO retrieve email addresses

        return newDocument;
        /*} else {
            throw new UnauthorizedClientException("anonymous user session");
        }*/
    }

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
    private User creatNewUserFromOIDCUserinfo(HttpServletRequest request, String issuer, OIDCUserinfoDto userInfoDto, String oidcToken) {
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
        if ("development".equals(env.getProperty("lds.app.instance"))) {
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
        enrichAuthoritiesWithStoredAuthorities(request, userInfoDto.getSub(), issuer + "/", userInfoDto, oidcToken, user, auth);

        return user;
    }

    private Role createNewUserRole(String newRoleName) {
        Role newUserRole = new Role();
        newUserRole.setName(newRoleName);
        return roleService.save(newUserRole);
    }

    private void enrichAuthoritiesWithStoredAuthorities(HttpServletRequest request, String sub, String issuer, OIDCUserinfoDto oidcUserinfoDto, String oidcToken, User user, Authentication auth) {
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
        user.getRoleList().forEach(role -> {
            newAuthorities.add(new SimpleGrantedAuthority(role.getName()));
        });
        if (oidcAuthority[0] == null) {
            newAuthorities.add(new SubjectIssuerGrantedAuthority(sub, issuer));
        } else {
            newAuthorities.add(oidcAuthority[0]);
        }
        OIDCAuthenticationToken token = new OIDCAuthenticationToken(sub, issuer, userInfo, newAuthorities, null, oidcToken, null);
        token.setDetails(new WebAuthenticationDetails(request));
        SecurityContextHolder.getContext().setAuthentication(token);
    }
}
