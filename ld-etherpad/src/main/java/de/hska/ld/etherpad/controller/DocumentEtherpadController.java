/*
 *  Code contributed to the Learning Layers project
 *  http://www.learning-layers.eu
 *  Development is partly funded by the FP7 Programme of the European
 *  Commission under Grant Agreement FP7-ICT-318209.
 *  Copyright (c) 2016, Karlsruhe University of Applied Sciences.
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

package de.hska.ld.etherpad.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import de.hska.ld.etherpad.dto.*;
import de.hska.ld.etherpad.persistence.domain.DocumentEtherpadInfo;
import de.hska.ld.etherpad.persistence.domain.UserEtherpadInfo;
import de.hska.ld.etherpad.service.DocumentEtherpadInfoService;
import de.hska.ld.etherpad.service.UserEtherpadInfoService;
import de.hska.ld.etherpad.util.Etherpad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
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
                    //String urlEncodedDocumentTitle = URLEncoder.encode(URLEncoder.encode(document.getTitle(), "UTF-8"), "UTF-8");
                    String groupPadTitle = UUID.randomUUID().toString();//StringUtils.left(urlEncodedDocumentTitle, 50);
                    while (groupPadTitle.endsWith("%")) {
                        groupPadTitle = groupPadTitle.substring(0, groupPadTitle.length() - 1);
                    }
                    if (mainSource != null) {
                        String discussionText = new String(mainSource, "UTF-8");
                        if (!"".equals(discussionText)) {
                            groupPadId = etherpadClient.createGroupPad(groupId, groupPadTitle);
                            //groupPadId = etherpadClient.createGroupPad(groupId, document.getTitle(), discussionText);
                            etherpadClient.setGroupPadContent(groupPadId, discussionText);
                            //setHTML(padID, html)
                        } else {
                            groupPadId = etherpadClient.createGroupPad(groupId, groupPadTitle);
                        }
                    } else {
                        groupPadId = etherpadClient.createGroupPad(groupId, groupPadTitle);
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

    @RequestMapping(method = RequestMethod.POST, value = "/etherpad/conversations")
    public Callable getConversations(@RequestBody EtherpadDocumentUpdateDto etherpadDocumentUpdateDto) {
        return () -> {
            if (env.getProperty("module.etherpad.apikey").equals(etherpadDocumentUpdateDto.getApiKey())) {
                String sessionId = etherpadDocumentUpdateDto.getAuthorId();
                UserEtherpadInfo userEtherpadInfo = userEtherpadInfoService.findBySessionId(sessionId);
                if (userEtherpadInfo == null) {
                    return new ResponseEntity<>("sessionID is invalid", HttpStatus.UNAUTHORIZED);
                }
                DocumentEtherpadInfo documentEtherpadInfo = documentEtherpadInfoService.findByGroupPadId(etherpadDocumentUpdateDto.getPadId());
                return userService.callAs(userEtherpadInfo.getUser(), () -> {
                    // retrieve all conversations the user has access to
                    Long documentId = documentEtherpadInfo.getDocument().getId();
                    int pageNumber = 0;
                    int pageSize = 10;
                    String sortDirection = "DESC";
                    String sortProperty = "createdAt";
                    Page<Document> documentPage = documentService.getDiscussionDocumentsPage(documentId, pageNumber, pageSize, sortDirection, sortProperty);
                    System.out.println(documentPage);
                    List<Document> documentList = documentPage.getContent();
                    List<DocumentInfo> documentInfoList = new ArrayList<DocumentInfo>();
                    documentList.forEach(d -> {
                        DocumentInfo documentInfo = new DocumentInfo();
                        documentInfo.setId(d.getId());
                        documentInfo.setTitle(d.getTitle());
                        documentInfoList.add(documentInfo);
                    });
                    return new ResponseEntity<>(documentInfoList, HttpStatus.OK);
                });
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        };
    }

    @RequestMapping(method = RequestMethod.POST, value = "/etherpad/conversationsForComments")
    public Callable getConversationsForComments(@RequestBody ConversationsForCommentsReqDto conversationsForCommentsReqDto) {
        return () -> {
            if (env.getProperty("module.etherpad.apikey").equals(conversationsForCommentsReqDto.getApiKey())) {
                String sessionId = conversationsForCommentsReqDto.getAuthorId();
                UserEtherpadInfo userEtherpadInfo = userEtherpadInfoService.findBySessionId(sessionId);
                if (userEtherpadInfo == null) {
                    return new ResponseEntity<>("sessionID is invalid", HttpStatus.UNAUTHORIZED);
                }
                ConversationsForCommentsReqDto temp = conversationsForCommentsReqDto;
                System.out.println(temp);
                DocumentEtherpadInfo documentEtherpadInfo = documentEtherpadInfoService.findByGroupPadId(conversationsForCommentsReqDto.getPadId());
                return userService.callAs(userEtherpadInfo.getUser(), () -> {
                    if (temp.getCommentIdList().size() > 0) {
                        CommentConversationDto commentConversationDto = new CommentConversationDto();
                        commentConversationDto.setCommentId(temp.getCommentIdList().get(0));
                        commentConversationDto.setConversationId("Test");
                        return new ResponseEntity<>(commentConversationDto, HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>("[]", HttpStatus.OK);
                    }
                });
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        };
    }

    @RequestMapping(method = RequestMethod.GET, value = "/etherpad/padValue")
    public Callable getCommentRange() {
        return () -> {
            String padValue = "{\"atext\":{\"text\":\"Test Hello World Greetings! Howdy!?\\n\\nTest Hello World Greetings! Howdy!?\\nTest Hello World Greetings! Howdy!?\\nTest Hello World Greetings! How\\nTest Hello World Greetings! Howdy!?\\nTest Hello World Greetings! Howdy!?\\nTest Hello World Greetings! Howdy!?\\nTest Hello World Greetings! Howdy!?\\nTest Hello World Greetings! Howdy!?\\nTest Hello World Greetings! Howdy!?\\nTest Hello World Greetings! Howdy!?\\nTest Hello World Greetings! Howdy!?\\nTest Hello World Greetings! Howdy!?\\nTest Hello World Greetings! Howdy!?\\nTest Hello World Greetings! Howdy!?\\nTest Hello World Greetings! Howdy!?\\nTest Hello World Greetings! Howdy!?\\nTest Hello World Greetings! Howdy!?\\nTest Hello World Greetings! Howdy!?\\nTest Hello World Greetings! Howdy!?Test Hello World Greetings! Howdy!Test Hello World Greetings! Howdy!Test Hello World Greetings! Howdy!Test Hello World Greetings! Howdy!Test Hello World Greetings! Howdy!Test Hello World Greetings! Howdy!Test Hello World Greetings! Howdy!Test Hello World Greetings! Howdy!Test Hello World Greetings! Howdy!Test Hello World Greetings! Howdy!Test Hello World Greetings! Howdy!Test Hello World Greetings! Howdy!Test Hello World Greetings! Howdy!Test Hello World Greetings! Howdy!Test Hello World Greetings! Howdy!Test Hello World Greetings! Howdy!Test Hello World Greetings! Howdy!Test Hello World Greetings! Howdy!\\n\\n\",\"attribs\":\"*0+5*0*5+5*0+6*1+5*1*2+1*1*4+4*1+1*0+2*0*9+2*0+2*1+1*3+1|1+1*1|2+11*1+z|1+1*1|5+4w*1+a*1*d+k*1|c+s6|1+1\"},\"pool\":{\"numToAttrib\":{\"0\":[\"author\",\"a.hGAAygbqEdeOn61q\"],\"1\":[\"author\",\"a.rbfAeHM4OgQoMFlR\"],\"2\":[\"bold\",\"true\"],\"3\":[\"author\",\"a.O9ItVBgCO18puRFb\"],\"4\":[\"italic\",\"true\"],\"5\":[\"comment\",\"c-QJdNmZaFtvODLB46\"],\"6\":[\"bold\",\"\"],\"7\":[\"italic\",\"\"],\"8\":[\"comment\",\"\"],\"9\":[\"comment\",\"c-OF96YYEMUYnOD8dj\"],\"10\":[\"insertorder\",\"first\"],\"11\":[\"lmkr\",\"1\"],\"12\":[\"pageBreak\",\"pageBreak\"],\"13\":[\"comment\",\"c-wOKOkPHQhlrz8PjQ\"]},\"nextNum\":14},\"head\":87,\"chatHead\":0,\"publicStatus\":false,\"passwordHash\":null,\"savedRevisions\":[]}";

            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            PadValueDto padValueDto = mapper.readValue(padValue, PadValueDto.class);
            //"c-OF96YYEMUYnOD8dj"

            String needleCommentId = "c-wOKOkPHQhlrz8PjQ";
            Map<String, String[]> numToAttrib = padValueDto.getPool().getNumToAttrib();

            long attributeId = getAttributeId(numToAttrib, needleCommentId);

            String attribs = padValueDto.getAtext().getAttribs();

            CommentRangeDto commentRangeDto = calculateCommentStartAndEnd(attribs, attributeId);
            commentRangeDto.setCommentedText("");
            if (commentRangeDto.getPosStart() != -1L && commentRangeDto.getPosEnd() != -1L) {
                commentRangeDto.setCommentedText(padValueDto.getAtext().getText().substring((int) commentRangeDto.getPosStart(), (int) commentRangeDto.getPosEnd()));
            }
            return new ResponseEntity<>(commentRangeDto, HttpStatus.OK);
        };
    }

    private CommentRangeDto calculateCommentStartAndEnd(String attribs, Long attributeId) {
        CommentRangeDto commentRangeDto = new CommentRangeDto();
        // split attribs by line breaks
        String[] lines = attribs.split("\\|");
        long currentPos = 0;
        long foundStartAt = -1;
        long foundEnd = -1;
        for (String line : lines) {
            String[] splittedAttribs = line.split("\\*");
            for (String splittedAttrib : splittedAttribs) {
                String[] attribLength = splittedAttrib.split("\\+");
                if (attribLength.length == 2) {
                    if (Long.parseLong(attribLength[0], 36) == attributeId) {
                        // TODO check if base 24
                        foundStartAt = currentPos;
                    }
                    String length = attribLength[1];
                    long parsedLength = Long.parseLong(length, 36);
                    currentPos += parsedLength;
                    if (Long.parseLong(attribLength[0], 36) == attributeId) {
                        foundEnd = currentPos;
                        break;
                    }
                }
            }
            if (foundEnd != -1) {
                break;
            }
        }

        commentRangeDto.setPosStart(foundStartAt);
        commentRangeDto.setPosEnd(foundEnd);
        return commentRangeDto;
    }

    private long getAttributeId(Map<String, String[]> numToAttrib, String commentId) {
        // TODO replace this by pattern matching
        Set<Map.Entry<String, String[]>> entrySet = numToAttrib.entrySet();
        for (Map.Entry<String, String[]> entry : entrySet) {
            String[] values = entry.getValue();
            for (String val : values) {
                if (commentId.equals(val)) {
                    return Long.parseLong(entry.getKey());
                }
            }
        }
        return -1;
    }
}
