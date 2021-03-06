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

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.util.Core;
import de.hska.ld.core.util.EscapeUtil;
import de.hska.ld.oidc.client.SSSClient;
import de.hska.ld.oidc.dto.*;
import de.hska.ld.oidc.persistence.domain.DocumentSSSInfo;
import de.hska.ld.oidc.service.DocumentSSSInfoService;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.LoggingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@RestController
@RequestMapping("/api/discussions")
public class DiscussionController {

    @Autowired
    private SSSClient sssClient;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentSSSInfoService documentSSSInfoService;

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/document/{documentId}/discussions/list")
    public Callable getDisscussionList(@PathVariable Long documentId) {
        return () -> {
            //Document document = documentService.findById(documentId);
            //if (document == null) {
            //    throw new ValidationException("documentId");
            //}

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            OIDCAuthenticationToken token = (OIDCAuthenticationToken) auth;
            SSSDiscsDto sssDiscsDto = sssClient.getDiscussionsForDocument(documentId, token.getAccessTokenValue());
            List<SSSDiscDto> discussionList = sssDiscsDto.getDiscs();

            List<String> preloadAttachmentIds = new ArrayList<>();
            for (SSSDiscDto discussion : discussionList) {
                List<SSSEntityDto> attachedEntities = discussion.getAttachedEntities();
                for (SSSEntityDto attachedEntity : attachedEntities) {
                    addEntityToPrefetchArray(preloadAttachmentIds, attachedEntity);
                }
                List<SSSQAEntryDto> entryList = discussion.getEntries();
                for (SSSQAEntryDto entry : entryList) {
                    List<SSSEntityDto> entryAttachedEntities = entry.getAttachedEntities();
                    for (SSSEntityDto entryAttachedEntity : entryAttachedEntities) {
                        addEntityToPrefetchArray(preloadAttachmentIds, entryAttachedEntity);
                    }
                }
            }
            SSSFileEntitiesDto sssFileEntitiesDto = fetchFileEntityInformation(sssClient, preloadAttachmentIds, token);

            if (sssFileEntitiesDto != null && sssFileEntitiesDto.getEntities() != null) {
                for (SSSFileEntityDto sssFileEntityDto : sssFileEntitiesDto.getEntities()) {
                    sssFileEntityDto.getId();
                    for (SSSDiscDto discussion : discussionList) {
                        List<SSSEntityDto> attachedEntities = discussion.getAttachedEntities();
                        for (SSSEntityDto attachedEntity : attachedEntities) {
                            if (sssFileEntityDto.getId().equals(attachedEntity.getId())) {
                                sssFileEntityDto.getFile().setFileIcon(getIconTypeFromFile(sssFileEntityDto));
                                attachedEntity.setFile(sssFileEntityDto.getFile());
                            }
                        }
                        List<SSSQAEntryDto> entryList = discussion.getEntries();
                        for (SSSQAEntryDto entry : entryList) {
                            List<SSSEntityDto> entryAttachedEntities = entry.getAttachedEntities();
                            for (SSSEntityDto entryAttachedEntity : entryAttachedEntities) {
                                if (sssFileEntityDto.getId().equals(entryAttachedEntity.getId())) {
                                    sssFileEntityDto.getFile().setFileIcon(getIconTypeFromFile(sssFileEntityDto));
                                    entryAttachedEntity.setFile(sssFileEntityDto.getFile());
                                }
                            }
                        }
                    }
                }
            }

            return new ResponseEntity<>(sssDiscsDto, HttpStatus.OK);
        };
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/fileEntity/{fileEntityId}/download")
    public Callable getFileDownloadLink(@PathVariable String fileEntityId) {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            OIDCAuthenticationToken token = (OIDCAuthenticationToken) auth;
            String downloadLink = null;
            if (sssClient.getSssAPIVersion() == 1) {
                downloadLink = sssClient.getSssServerAddress() +
                        "/files/files/download?" +
                        "file=" + fileEntityId + "&key=" + token.getAccessTokenValue();
            } else {
                downloadLink = sssClient.getSssServerAddress() +
                        "/rest/files/download?" +
                        "file=" + fileEntityId + "&key=" + token.getAccessTokenValue();
            }
            try {
                LoggingContext.put("user_email", EscapeUtil.escapeJsonForLogging(Core.currentUser().getEmail()));
                LoggingContext.put("sssFileEntityId", EscapeUtil.escapeJsonForLogging(fileEntityId.toString()));
                Logger.trace("User downloads file from discussions.");
            } catch (Exception e) {
                Logger.error(e);
            } finally {
                LoggingContext.clear();
            }
            return new ResponseEntity<>(downloadLink, HttpStatus.OK);
        };
    }

    /**
     * @param documentId
     * @param discRequestDto {
     *                       description: ""
     *                       label: "Test"
     *                       tags: []
     *                       }
     * @return
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/document/{documentId}/discussion")
    public ResponseEntity createDiscussion(@PathVariable String documentId, @RequestBody SSSCreateDiscRequestDto discRequestDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OIDCAuthenticationToken token = (OIDCAuthenticationToken) auth;
        SSSCreateDiscResponseDto sssCreateDiscResponseDto = null;
        // remove tag list because the sss doesn't know how to process this
        List<String> tagList = discRequestDto.getTags();
        discRequestDto.setTags(null);

        try {
            String episodeId = null;
            try {
                Document document = documentService.findById(Long.parseLong(documentId));
                if (document != null) {
                    DocumentSSSInfo documentSSSInfo = documentSSSInfoService.getDocumentSSSInfo(document);
                    if (documentSSSInfo != null) {
                        episodeId = documentSSSInfo.getEpisodeId();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            sssCreateDiscResponseDto = sssClient.createDiscussion(documentId, discRequestDto, token.getAccessTokenValue(), episodeId);
            try {
                LoggingContext.put("user_email", EscapeUtil.escapeJsonForLogging(Core.currentUser().getEmail()));
                LoggingContext.put("documentId", EscapeUtil.escapeJsonForLogging(documentId.toString()));
                LoggingContext.put("sssDiscussionLabel", EscapeUtil.escapeJsonForLogging(sssCreateDiscResponseDto.getDisc()));
                Logger.trace("User created discussion.");
            } catch (Exception e) {
                Logger.error(e);
            } finally {
                LoggingContext.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (tagList != null && tagList.size() > 0) {
            for (String tag : tagList) {
                try {
                    sssClient.addTagTo(sssCreateDiscResponseDto.getDisc(), tag, token.getAccessTokenValue());
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/discussion/comment")
    public ResponseEntity createCommentForDiscussion(@RequestBody SSSEntryForDiscussionRequestDto entryForDiscRequestDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OIDCAuthenticationToken token = (OIDCAuthenticationToken) auth;
        SSSEntryForDiscussionResponseDto sssEntryForDiscussionResponseDto = null;
        // remove tag list because the sss doesn't know how to process this
        List<String> tagList = entryForDiscRequestDto.getTags();
        entryForDiscRequestDto.setTags(null);
        try {
            sssEntryForDiscussionResponseDto = sssClient.createEntryForDiscussion(entryForDiscRequestDto, token.getAccessTokenValue());
            try {
                LoggingContext.put("user_email", EscapeUtil.escapeJsonForLogging(Core.currentUser().getEmail()));
                LoggingContext.put("sssDiscussionLabel", EscapeUtil.escapeJsonForLogging(sssEntryForDiscussionResponseDto.getDisc()));
                LoggingContext.put("sssEntryLabel", EscapeUtil.escapeJsonForLogging(sssEntryForDiscussionResponseDto.getEntry()));
                Logger.trace("User created comment for discussion.");
            } catch (Exception e) {
                Logger.error(e);
            } finally {
                LoggingContext.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (tagList != null && tagList.size() > 0) {
            for (String tag : tagList) {
                try {
                    sssClient.addTagTo(sssEntryForDiscussionResponseDto.getEntry(), tag, token.getAccessTokenValue());
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private SSSFileEntitiesDto fetchFileEntityInformation(SSSClient sssClient, List<String> attachmentIds, OIDCAuthenticationToken token) {
        SSSFileEntitiesDto fileEntitiesDto = null;
        try {
            fileEntitiesDto = sssClient.getFileEntity(attachmentIds, token.getAccessTokenValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileEntitiesDto;
    }

    private void addEntityToPrefetchArray(List<String> prefetchArray, SSSEntityDto entity) {
        if (!("placeholder".equals(entity.getType()) && "evernoteNotebook".equals(entity.getType()))) {
            if ("evernoteResource".equals(entity.getType()) || "evernoteNote".equals(entity.getType())) {
                prefetchArray.add(entity.getId());
            }
        }
    }

    private String getIconTypeFromFile(SSSFileEntityDto fileEntity) {
        //var mimeType = fileEntity.mimeType;
        // XXX This is due to issues with mimeType
        String mimeType = fileEntity.getFile().getMimeType();
        String name = fileEntity.getFile().getType();

        if (mimeType != null && !"".equals(mimeType)) {
            switch (mimeType) {
                case "application/pdf":
                    name = "filePdf";
                    break;
                case "image/png":
                case "image/jpeg":
                case "image/x-icon":
                case "image/gif":
                case "image/svg+xml":
                case "image/bmp":
                case "image/tiff":
                    name = "fileImage";
                    break;
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                case "application/msword":
                    name = "fileDoc";
                    break;
                case "application/vnd.ms-excel":
                case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                    name = "fileSpreadsheet";
                    break;
                case "application/vnd.ms-powerpoint":
                case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                    name = "filePresentation";
                    break;
            }
        } else {
            switch (fileEntity.getId().substring(fileEntity.getId().length() - 4).toLowerCase()) {
                case ".pdf":
                    name = "filePdf";
                    break;
                case ".png":
                case ".jpg":
                case "jpeg":
                case ".ico":
                case ".gif":
                case ".svg":
                case ".bmp":
                case ".tif":
                case "tiff":
                    name = "fileImage";
                    break;
                case "docx":
                case ".doc":
                    name = "fileDoc";
                    break;
                case ".xls":
                case "xlsx":
                    name = "fileSpreadsheet";
                    break;
                case ".ppt":
                case "pptx":
                    name = "filePresentation";
                    break;
            }
        }

        return name;
    }
}
