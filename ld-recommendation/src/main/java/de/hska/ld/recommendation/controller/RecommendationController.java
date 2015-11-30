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

package de.hska.ld.recommendation.controller;

import de.hska.ld.content.persistence.domain.Access;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import de.hska.ld.recommendation.client.SSSClient;
import de.hska.ld.recommendation.dto.*;
import de.hska.ld.recommendation.service.DocumentRecommInfoService;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private SSSClient sssClient;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentRecommInfoService documentRecommInfoService;

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/initial/document")
    public ResponseEntity<List<Document>> performInitialSSSUpdateDocuments() throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OIDCAuthenticationToken token = (OIDCAuthenticationToken) auth;
        return new ResponseEntity<List<Document>>(documentRecommInfoService.addMissingRecommendationUpdatesDocuments(token.getAccessTokenValue()), HttpStatus.OK);
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/initial/user")
    public ResponseEntity<List<User>> performInitialSSSUpdateUsers() throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OIDCAuthenticationToken token = (OIDCAuthenticationToken) auth;
        return new ResponseEntity<List<User>>(documentRecommInfoService.addMissingRecommendationUpdatesUsers(token.getAccessTokenValue()), HttpStatus.OK);
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{documentId}")
    @Transactional(readOnly = false, noRollbackFor = NoSuchElementException.class)
    public ResponseEntity<LDRecommendationDto> getRecommendations(@PathVariable Long documentId) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OIDCAuthenticationToken token = (OIDCAuthenticationToken) auth;
        SSSRecommResponseDto sssRecommResponseDto = sssClient.retrieveRecommendations(documentId, token.getAccessTokenValue());
        if (sssRecommResponseDto == null) {
            return new ResponseEntity<LDRecommendationDto>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        List<SSSUserRecommendationDto> sssUserRecommendationDtoList = sssRecommResponseDto.getUsers();
        List<LDRecommendationUserDto> userIdList = new ArrayList<>();
        List<LDRecommendationDocumentDto> documentIdList = new ArrayList<>();
        sssUserRecommendationDtoList.forEach(ur -> {
            String userUri = ur.getUser().getId();
            // 1. Determine if the result is a user or a document
            if (userUri != null && userUri.contains("/user/")) {
                // 1.1 it is a user instance
                String[] splittedUserId = userUri.split("/user/");
                String userId = splittedUserId[splittedUserId.length - 1];
                Double likelihood = ur.getLikelihood();
                try {
                    LDRecommendationUserDto ldRecommendationUserDto = new LDRecommendationUserDto();
                    ldRecommendationUserDto.setUserId(Long.parseLong(userId));
                    ldRecommendationUserDto.setLikelihood(likelihood);
                    userIdList.add(ldRecommendationUserDto);
                } catch (Exception e) {
                    //
                }
            } else if (userUri != null && userUri.contains("/document/")) {
                // 1.2 it is a document instance
                String[] splittedDocumentId = userUri.split("/document/");
                String documentIdRecommended = splittedDocumentId[splittedDocumentId.length - 1];
                Double likelihood = ur.getLikelihood();
                try {
                    LDRecommendationDocumentDto ldRecommendationDocumentDto = new LDRecommendationDocumentDto();
                    ldRecommendationDocumentDto.setDocumentId(Long.parseLong(documentIdRecommended));
                    ldRecommendationDocumentDto.setLikelihood(likelihood);
                    documentIdList.add(ldRecommendationDocumentDto);
                } catch (Exception e) {
                    //
                }
            }
        });

        // fetch the related data sets from the living documents db
        List<LDRecommendationUserDto> userList = documentRecommInfoService.fetchUserRecommendationDatasets(userIdList);
        List<LDRecommendationDocumentDto> documentList = documentRecommInfoService.fetchDocumentRecommendationDatasets(documentIdList);

        try {
            Collections.sort(userList);
            Collections.sort(documentList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Long currenUserId = Core.currentUser().getId();
        User initialLoadUser = userService.findByUsername("aur0rp3");

        // filter out current user
        LDRecommendationUserDto found1 = null;
        LDRecommendationUserDto foundInit = null;
        List<LDRecommendationUserDto> likelihood0Users = new ArrayList<LDRecommendationUserDto>();
        for (LDRecommendationUserDto userRecomm : userList) {
            /*if (0d == userRecomm.getLikelihood()) {
                likelihood0Users.add(userRecomm);
            }*/
            if (currenUserId.equals(userRecomm.getUserId())) {
                found1 = userRecomm;
            }
            if (initialLoadUser != null && initialLoadUser.getId().equals(userRecomm.getUserId())) {
                foundInit = userRecomm;
            }
        }
        if (found1 != null) {
            userList.remove(found1);
        }
        if (foundInit != null) {
            userIdList.remove(foundInit);
        }
        if (likelihood0Users.size() > 0) {
            userList.removeAll(likelihood0Users);
        }

        // filter out current document
        LDRecommendationDocumentDto found = null;
        List<LDRecommendationDocumentDto> likelihood0Documents = new ArrayList<LDRecommendationDocumentDto>();
        for (LDRecommendationDocumentDto documentRecomm : documentList) {
            if (0d == documentRecomm.getLikelihood()) {
                likelihood0Documents.add(documentRecomm);
            }
            if (documentId.equals(documentRecomm.getDocumentId())) {
                found = documentRecomm;
            }
        }
        if (found != null) {
            documentList.remove(found);
        }
        if (likelihood0Documents.size() > 0) {
            documentList.removeAll(likelihood0Documents);
        }

        // filter out documents the current user has no access to
        List<LDRecommendationDocumentDto> noPermissionDocuments = new ArrayList<LDRecommendationDocumentDto>();
        for (LDRecommendationDocumentDto documentRecomm : documentList) {
            Long documentIdPermissionCheck = documentRecomm.getDocumentId();
            Document document = documentService.findById(documentIdPermissionCheck);
            if (!documentService.checkPermissionSave(document, Access.Permission.READ)) {
                noPermissionDocuments.add(documentRecomm);
            }
        }
        if (noPermissionDocuments.size() > 0) {
            documentList.removeAll(noPermissionDocuments);
        }

        LDRecommendationDto ldRecommendationDto = new LDRecommendationDto();
        ldRecommendationDto.setUserList(userList);
        ldRecommendationDto.setDocumentList(documentList);
        ldRecommendationDto.setDocumentId(documentId);

        return new ResponseEntity<LDRecommendationDto>(ldRecommendationDto, HttpStatus.OK);
    }

}
