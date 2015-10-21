package de.hska.ld.etherpad.controller;

import de.hska.ld.content.dto.EtherpadDocumentUpdateDto;
import de.hska.ld.content.persistence.domain.Access;
import de.hska.ld.content.persistence.domain.Attachment;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.exception.NotFoundException;
import de.hska.ld.core.exception.ValidationException;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import de.hska.ld.etherpad.client.EtherpadClient;
import de.hska.ld.etherpad.persistence.domain.DocumentEtherpadInfo;
import de.hska.ld.etherpad.persistence.domain.UserEtherpadInfo;
import de.hska.ld.etherpad.service.DocumentEtherpadInfoService;
import de.hska.ld.etherpad.service.UserEtherpadInfoService;
import de.hska.ld.etherpad.util.Etherpad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.Callable;

@RestController
@RequestMapping(Etherpad.RESOURCE_DOCUMENT_ETHERPAD)
public class DocumentEtherpadController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserEtherpadInfoService userEtherpadInfoService;

    @Autowired
    private DocumentEtherpadInfoService documentEtherpadInfoService;

    @Autowired
    private EtherpadClient etherpadClient;

    @Autowired
    private Environment env;

    private String etherpadEndpoint = null;

    @PostConstruct
    public void postConstruct() {
        etherpadEndpoint = env.getProperty("module.etherpad.endpoint");
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/edit/{documentId}")
    //@Transactional(readOnly = true)
    public Callable editDocumentContent(HttpServletResponse response, @PathVariable Long documentId) {
        return () -> {
            Document document = documentService.findById(documentId);
            boolean readOnly = false;

            // check if the User is allowed to access the current Document
            if (document != null) {
                documentService.checkPermission(document, Access.Permission.READ);
                try {
                    documentService.checkPermission(document, Access.Permission.WRITE);
                } catch (Exception e) {
                    readOnly = true;
                }
            } else {
                throw new NotFoundException("id");
            }

            // for the given User check whether there is an AuthorId registered in Etherpad
            UserEtherpadInfo firstUserEtherPadInfoCheck = userEtherpadInfoService.getUserEtherpadInfoForCurrentUser();
            String authorId = null;
            if (firstUserEtherPadInfoCheck != null) {
                authorId = firstUserEtherPadInfoCheck.getAuthorId();
            }

            //  look up if there is an existing AuthorId associated with the current user
            if (authorId == null) {

                // if there is no AuthorId present register an AuthorId for the current User
                authorId = etherpadClient.createAuthor(Core.currentUser().getFullName());
                userEtherpadInfoService.storeAuthorIdForCurrentUser(authorId);
            }

            // is the GroupPad available for the Document :
            String groupPadId = documentEtherpadInfoService.getGroupPadIdForDocument(document);
            if (groupPadId == null && !"".equals(groupPadId)) {
                //  otherwise create a GroupPad
                String groupId = etherpadClient.createGroup();
                Attachment mainContent = document.getAttachmentList().get(0);
                byte[] mainSource = mainContent.getSource();
                try {
                    if (mainSource != null) {
                        String discussionText = new String(mainSource, "UTF-8");
                        if (!"".equals(discussionText)) {
                            groupPadId = etherpadClient.createGroupPad(groupId, document.getTitle());
                            //groupPadId = etherpadClient.createGroupPad(groupId, document.getTitle(), discussionText);
                            etherpadClient.setGroupPadContent(groupPadId, discussionText);
                            //setHTML(padID, html)
                        } else {
                            groupPadId = etherpadClient.createGroupPad(groupId, document.getTitle());
                        }
                    } else {
                        groupPadId = etherpadClient.createGroupPad(groupId, document.getTitle());
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                //  groupPad is available associate GroupPadId for the Document
                documentEtherpadInfoService.storeGroupPadIdForDocument(groupPadId, document);
            }

            String readOnlyId = null;
            if (readOnly) {
                readOnlyId = documentEtherpadInfoService.getReadOnlyIdForDocument(document);
                if (readOnlyId == null) {
                    readOnlyId = etherpadClient.getReadOnlyID(groupPadId);
                    if (readOnlyId == null) {
                        throw new ValidationException("Read only id is null"); // TODO change exception type
                    } else {
                        documentEtherpadInfoService.storeReadOnlyIdForDocument(readOnlyId, document);
                    }
                }
            }

            // create a session between Author and GroupPad
            String groupId = groupPadId.split("\\$")[0];
            long currentTime = System.currentTimeMillis() / 1000L; // current time
            long validUntil = currentTime + 86400L;

            String sessionId = null;
            UserEtherpadInfo userEtherpadInfo = userEtherpadInfoService.getUserEtherpadInfoForCurrentUser();
            sessionId = userEtherpadInfo.getSessionId();
            Long currentValidUntil = userEtherpadInfo.getValidUntil();

            // retrieve sessionID from db if available
            boolean newSessionRequired = false;
            if (sessionId == null) {
                newSessionRequired = true;
            } else {
                boolean isStillValid = false;
                // check if valid until is still valid for more than 3h
                // check if sessionID is still valid (valid for more than 3h)
                /*boolean sameGroupId = userEtherpadInfo.getGroupId().equals(groupId);
                if (sameGroupId && userEtherpadInfo.getGroupId().equals(groupId) && currentValidUntil - currentTime >= 10800) {
                    // if sessionID is still valid longer than 3h
                    // then send the sessionID to the client
                    isStillValid = true;
                } else if (currentValidUntil - currentTime < 10800) {
                    newSessionRequired = true;
                } else if (isStillValid) {*/
                    // check if the session still exists on the etherpad server (GET)
                isStillValid = etherpadClient.checkIfSessionStillValid(currentTime, sessionId, groupId);
                if (!isStillValid) {
                    newSessionRequired = true;
                }
                //}
            }
            if (newSessionRequired) {
                sessionId = etherpadClient.createSession(groupId, authorId, validUntil);

                // store the sessionID into UserEtherpadInfo object
                // store the validUntil value also
                User currentUser = Core.currentUser();
                User dbUser = userService.findById(currentUser.getId());
                userEtherpadInfoService.storeSessionForUser(sessionId, groupId, validUntil, userEtherpadInfo);
            }

            // we need return types, cookie with sessionId and the URL of Etherpads Pad
            javax.servlet.http.Cookie myCookie = new javax.servlet.http.Cookie("sessionID", sessionId);
            myCookie.setPath("/");
            if (!"localhost".equals(env.getProperty("module.core.oidc.server.endpoint.main.domain"))) {
                myCookie.setDomain(env.getProperty("module.core.oidc.server.endpoint.main.domain"));
            }
            response.addCookie(myCookie);
            // return Etherpad URL path
            String padURL = null;
            if (readOnly) {
                padURL = etherpadEndpoint + "/p/" + readOnlyId;
            } else {
                padURL = etherpadEndpoint + "/p/" + groupPadId;
            }

            return new ResponseEntity<>(padURL, HttpStatus.CREATED);
        };
    }


    @RequestMapping(method = RequestMethod.POST, value = "/etherpad/update")
    public Callable updateDocumentThroughEtherpad(@RequestBody EtherpadDocumentUpdateDto etherpadDocumentUpdateDto) {
        return () -> {
            if (env.getProperty("module.etherpad.apikey").equals(etherpadDocumentUpdateDto.getApiKey())) {
                String authorId = etherpadDocumentUpdateDto.getAuthorId();
                UserEtherpadInfo userEtherpadInfo = userEtherpadInfoService.findByAuthorId(authorId);
                DocumentEtherpadInfo documentEtherpadInfo = documentEtherpadInfoService.findByGroupPadId(etherpadDocumentUpdateDto.getPadId());
                documentService.updatedByUser(userEtherpadInfo.getUser(), documentEtherpadInfo.getDocument());
                System.out.println(userEtherpadInfo);
                System.out.println(documentEtherpadInfo);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        };
    }
}
