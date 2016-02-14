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

package de.hska.ld.oidc.listeners;

import de.hska.ld.content.events.document.DocumentCreationEvent;
import de.hska.ld.content.events.document.DocumentReadEvent;
import de.hska.ld.content.events.document.DocumentSharingEvent;
import de.hska.ld.content.persistence.domain.Access;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.events.user.UserFirstLoginEvent;
import de.hska.ld.core.events.user.UserLoginEvent;
import de.hska.ld.core.exception.UserNotAuthorizedException;
import de.hska.ld.core.logging.ExceptionLogger;
import de.hska.ld.core.persistence.domain.ExceptionLogEntry;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.oidc.client.SSSClient;
import de.hska.ld.oidc.client.exception.AuthenticationNotValidException;
import de.hska.ld.oidc.client.exception.CreationFailedException;
import de.hska.ld.oidc.dto.*;
import de.hska.ld.oidc.persistence.domain.UserSSSInfo;
import de.hska.ld.oidc.persistence.domain.UserSharingBuffer;
import de.hska.ld.oidc.service.UserSSSInfoService;
import de.hska.ld.oidc.service.UserSharingBufferService;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedClientException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Component("ld-oidc-LDToSSSEventListener")
public class LDToSSSEventListener {

    @Autowired
    private SSSClient sssClient;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserSSSInfoService userSSSInfoService;

    @Autowired
    private ExceptionLogger exceptionLogger;

    @Autowired
    private UserSharingBufferService userSharingBufferService;

    @Async
    @EventListener
    public void handleDocumentReadEvent(DocumentReadEvent event) throws IOException, CreationFailedException {
        Document document = (Document) event.getSource();
        System.out.println("LDToSSSEventListener: Reading document=" + document.getId() + ", title=" + document.getTitle());
        document = createAndShareLDocWithSSSUsers(document, "READ", event.getAccessToken(), null);
        event.setResultDocument(document);
    }

    @Async
    @EventListener
    public void handleDocumentCreationEvent(DocumentCreationEvent event) throws IOException, CreationFailedException {
        Document newDocument = (Document) event.getSource();
        System.out.println("LDToSSSEventListener: Creating document=" + newDocument.getId() + ", title=" + newDocument.getTitle());
        newDocument = createAndShareLDocWithSSSUsers(newDocument, "WRITE", event.getAccessToken(), null);
        SSSCreateDiscRequestDto sssCreateDiscRequestDto = new SSSCreateDiscRequestDto();
        sssCreateDiscRequestDto.setLabel(newDocument.getTitle());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OIDCAuthenticationToken token = (OIDCAuthenticationToken) auth;
        try {
            SSSCreateDiscResponseDto result = sssClient.createDiscussion(String.valueOf(newDocument.getId()), sssCreateDiscRequestDto, token.getAccessTokenValue());
            String disc = result.getDisc();
            System.out.println(disc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        event.setResultDocument(newDocument);
    }

    @Async
    @EventListener
    public void handleDocumentSharingEvent(DocumentSharingEvent event) throws IOException, CreationFailedException {
        Document document = (Document) event.getSource();
        System.out.println("LDToSSSEventListener: Sharing document=" + document.getId() + ", title=" + document.getTitle());
        document = createAndShareLDocWithSSSUsers(document, "READ", event.getAccessToken(), event.getAccessList());
        event.setResultDocument(document);
    }

    @Async
    @EventListener
    public void handleLoginEvent(UserLoginEvent event) throws IOException {
        User user = (User) event.getSource();
        checkIfSSSUserInfoIsKnown(user, event.getAccessToken());
    }

    @Async
    @EventListener
    public void handleFirstLoginEvent(UserFirstLoginEvent event) throws IOException {
        User user = (User) event.getSource();
        sharePreviouslySharedDocumentsWithTheNewUser(user, event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void sharePreviouslySharedDocumentsWithTheNewUser(User user, UserFirstLoginEvent event) {
        UserSharingBuffer userSharingBuffer = userSharingBufferService.findByEmail(user.getEmail());
        if (userSharingBuffer == null) {
            userSharingBuffer = userSharingBufferService.findBySubAndIssuer(user.getSubId(), user.getIssuer());
        }
        if (userSharingBuffer != null) {
            String userIds = user.getId().toString();
            if (!"".equals(userIds)) {
                Document dbDocument = documentService.findById(userSharingBuffer.getDocumentId());
                documentService.addAccessWithoutTransactional(dbDocument.getId(), userIds, userSharingBuffer.getPermissionString());
                try {
                    System.out.println("LDToSSSEventListener: Sharing document=" + dbDocument.getId() + ", title=" + dbDocument.getTitle());
                    createAndShareLDocWithSSSUsers(dbDocument, "READ", event.getAccessToken(), null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                userSharingBufferService.removeUserSharingBuffer(userSharingBuffer.getId());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void checkIfSSSUserInfoIsKnown(User user, String accessTokenValue) throws IOException {
        user = userService.findById(user.getId());
        UserSSSInfo userSSSInfo = userSSSInfoService.findByUser(user);
        // if the sss user id is already known to the server do nothing
        if (userSSSInfo == null) {
            // else authenticate towards the sss to retrieve the sss user id
            // and save that user id in the ldocs database
            SSSAuthDto sssAuthDto = null;
            try {
                sssAuthDto = sssClient.authenticate(accessTokenValue);
                String sssUserId = sssAuthDto.getUser();
                userSSSInfoService.addUserSSSInfo(user.getId(), sssUserId);
            } catch (UserNotAuthorizedException e) {
                e.printStackTrace();
                throw new UnauthorizedClientException("oidc token invalid");
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Document createAndShareLDocWithSSSUsers(Document document, String cmd, String accessToken, List<Access> accessList) throws IOException, CreationFailedException {
        // Create the document as well in the SSS
        document = documentService.findById(document.getId());
        if (accessList == null) {
            accessList = document.getAccessList();
        }
        List<String> emailAddressesThatHaveAccess = new ArrayList<>();
        for (Access access : accessList) {
            emailAddressesThatHaveAccess.add(access.getUser().getEmail());
        }
        // check if the living document is already known to the SSS
        //Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //OIDCAuthenticationToken token = (OIDCAuthenticationToken) auth;
        boolean isAlreadyKnownToSSS = false;
        String sssLivingDocId = null;
        Long newDocumentId = document.getId();
        try {
            SSSLivingDocResponseDto documentFoundInSSS = sssClient.getLDocById(newDocumentId, accessToken);
            if (documentFoundInSSS != null) {
                SSSLivingdoc documentFoundInSSSLDoc = documentFoundInSSS.getLivingDoc();
                if (documentFoundInSSSLDoc != null && documentFoundInSSSLDoc.getId() != null) {
                    isAlreadyKnownToSSS = true;
                } else if ("READ".equals(cmd)) {
                    exceptionLogger.log("createAndShareLDocWithSSSUsers (READ)", "Could not find the document in the SSS!", ExceptionLogEntry.LogLevel.FATAL);
                }
            } else if ("READ".equals(cmd)) {
                exceptionLogger.log("createAndShareLDocWithSSSUsers (READ)", "Could not find the document in the SSS!", ExceptionLogEntry.LogLevel.FATAL);
            }
            if (!isAlreadyKnownToSSS) {
                // create the living document in the SSS
                SSSLivingdocsResponseDto sssLivingdocsResponseDto2 = null;
                sssLivingdocsResponseDto2 = sssClient.createDocument(document, null, accessToken);
                sssLivingDocId = sssLivingdocsResponseDto2.getLivingDoc();
                if (sssLivingDocId == null) {
                    throw new CreationFailedException(newDocumentId);
                }
            }
            // Retrieve users/emails that have access to this living document declared by the SSS
            SSSLivingDocResponseDto sssLivingdocsResponseDto = sssClient.getLDocEmailsById(newDocumentId, accessToken);
            SSSLivingdoc sssLivingDoc = sssLivingdocsResponseDto.getLivingDoc();
            if (sssLivingDoc != null && sssLivingDoc.getUsers() != null) {
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                // TODO share living documents in the sss with all users that are
                // present within living documents but not within the sss
                // after the sharing the other way round happened
                List<String> checkedEmailAddresses = new ArrayList<>();
                for (SSSUserDto userDto : sssLivingDoc.getUsers()) {
                    boolean found = emailAddressesThatHaveAccess.contains(userDto.getLabel());
                    checkedEmailAddresses.add(userDto.getLabel());
                    if (!found) {
                        User user = userService.findByEmail(userDto.getLabel());
                        if (user != null && user.getId() != null && document.getCreator() != null &&
                                document.getCreator().getId() != null && !user.getId().equals(document.getCreator().getId())) {
                            if (first) {
                                sb.append(user.getId());
                                first = false;
                            } else {
                                sb.append(";").append(user.getId());
                            }
                        }
                    }
                }
                String userIds = sb.toString();
                try {
                    if (!"".equals(userIds)) {
                        Document dbDocument = documentService.findById(document.getId());
                        dbDocument = documentService.addAccessWithoutTransactional(dbDocument.getId(), userIds, "READ;WRITE");
                        dbDocument.getAttachmentList().size();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                emailAddressesThatHaveAccess.removeAll(checkedEmailAddresses);
                if (emailAddressesThatHaveAccess.size() > 0) {
                    List<String> sssUserIdsTheLDocIsNotSharedWith = new ArrayList<>();
                    emailAddressesThatHaveAccess.forEach(email -> {
                        UserSSSInfo sssUserInfo = userSSSInfoService.findByUserEmail(email);
                        if (sssUserInfo != null) {
                            sssUserIdsTheLDocIsNotSharedWith.add(sssUserInfo.getSssUserId());
                        } else {
                            System.out.println("No sss user id found for email address=" + email);
                        }
                    });
                    sssClient.shareLDocWith(document.getId(), sssUserIdsTheLDocIsNotSharedWith, accessToken);
                }
            }
        } catch (AuthenticationNotValidException eAuth) {
            eAuth.printStackTrace();
        }
        return document;
    }
}