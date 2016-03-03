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

package de.hska.ld.oidc.controller;

import de.hska.ld.content.persistence.domain.Access;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.exception.NotFoundException;
import de.hska.ld.core.util.Core;
import de.hska.ld.oidc.client.SSSClient;
import de.hska.ld.oidc.dto.SSSCircleInfoWrapper;
import de.hska.ld.oidc.persistence.domain.DocumentSSSInfo;
import de.hska.ld.oidc.service.DocumentSSSInfoService;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.Callable;

@RestController
@RequestMapping("/api/sssinfo")
public class DocumentSSSInfoController {

    @Autowired
    private DocumentSSSInfoService documentSSSInfoService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private SSSClient sssClient;

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/document/{documentId}/episode")
    @Transactional
    public Callable getEpisodeId(@PathVariable Long documentId) {
        return () -> {
            Document document = documentService.findById(documentId);
            if (document == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            DocumentSSSInfo documentSSSInfo = documentSSSInfoService.getDocumentSSSInfo(document);
            if (documentSSSInfo == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(documentSSSInfo, HttpStatus.OK);
        };
    }

    @RequestMapping(method = RequestMethod.GET, value = "/episode/circleinfo")
    @Transactional(readOnly = false, rollbackFor = RuntimeException.class)
    public Callable getEpisodeCircleInformation(@RequestParam Long documentId) throws IOException, ServletException {
        return () -> {
            Document document = documentService.findById(documentId);
            documentService.checkPermission(document, Access.Permission.READ);

            DocumentSSSInfo documentSSSInfo = documentSSSInfoService.getDocumentSSSInfo(document);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            OIDCAuthenticationToken token = (OIDCAuthenticationToken) auth;
            String episodeId = null;
            if (documentSSSInfo != null) {
                episodeId = documentSSSInfo.getEpisodeId();
            } else {
                return new NotFoundException("documentId");
            }
            if (episodeId == null) {
                return new NotFoundException("episodeId not available");
            }
            SSSCircleInfoWrapper sssCircleInfoWrapper = sssClient.getCircleInformation(episodeId, token.getAccessTokenValue());
            if (sssCircleInfoWrapper != null) {
                return new ResponseEntity<>(sssCircleInfoWrapper, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        };
    }
}
