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

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.util.Core;
import de.hska.ld.recommendation.client.SSSClient;
import de.hska.ld.recommendation.dto.LDRecommendationDto;
import de.hska.ld.recommendation.dto.SSSRecommResponseDto;
import de.hska.ld.recommendation.dto.SSSUserRecommendationDto;
import de.hska.ld.recommendation.service.DocumentRecommInfoService;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private SSSClient sssClient;

    @Autowired
    private DocumentRecommInfoService documentRecommInfoService;

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{documentId}/users")
    public ResponseEntity<Document> getExpertRecommendations(@PathVariable Long documentId) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OIDCAuthenticationToken token = (OIDCAuthenticationToken) auth;
        sssClient.retrieveRecommendations(documentId, token.getAccessTokenValue());
        return null;
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{documentId}")
    public ResponseEntity<LDRecommendationDto> getRecommendations(@PathVariable Long documentId) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OIDCAuthenticationToken token = (OIDCAuthenticationToken) auth;
        SSSRecommResponseDto sssRecommResponseDto = sssClient.retrieveRecommendations(documentId, token.getAccessTokenValue());
        if (sssRecommResponseDto == null) {
            return new ResponseEntity<LDRecommendationDto>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        List<SSSUserRecommendationDto> sssUserRecommendationDtoList = sssRecommResponseDto.getUsers();
        List<Long> userIdList = new ArrayList<>();
        List<Long> documentIdList = new ArrayList<>();
        sssUserRecommendationDtoList.forEach(ur -> {
            String userUri = ur.getUser().getId();
            // 1. Determine if the result is a user or a document
            if (userUri != null && userUri.contains("/user/")) {
                // 1.1 it is a user instance
                String[] splittedUserId = userUri.split("/user/");
                String userId = splittedUserId[splittedUserId.length - 1];
                try {
                    userIdList.add(Long.parseLong(userId));
                } catch (Exception e) {
                    //
                }
            } else if (userUri != null && userUri.contains("/document/")) {
                // 1.2 it is a document instance
                String[] splittedDocumentId = userUri.split("/document/");
                String documentIdRecommended = splittedDocumentId[splittedDocumentId.length - 1];
                try {
                    documentIdList.add(Long.parseLong(documentIdRecommended));
                } catch (Exception e) {
                    //
                }
            }
            // TODO 2. Add the likelihood to the result object
            //Double likelihood = ur.getLikelihood();
        });

        // fetch the related data sets from the living documents db
        List<User> userList = documentRecommInfoService.fetchUserRecommendationDatasets(userIdList);
        List<Document> documentList = documentRecommInfoService.fetchDocumentRecommendationDatasets(documentIdList);

        LDRecommendationDto ldRecommendationDto = new LDRecommendationDto();
        ldRecommendationDto.setUserList(userList);
        ldRecommendationDto.setDocumentList(documentList);
        ldRecommendationDto.setDocumentId(documentId);

        return new ResponseEntity<LDRecommendationDto>(ldRecommendationDto, HttpStatus.OK);
    }

}
