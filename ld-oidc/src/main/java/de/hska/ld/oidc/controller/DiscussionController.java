package de.hska.ld.oidc.controller;

import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.util.Core;
import de.hska.ld.oidc.client.SSSClient;
import de.hska.ld.oidc.dto.*;
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
import java.util.concurrent.Callable;

@RestController
@RequestMapping("/api/discussions")
public class DiscussionController {

    @Autowired
    private DocumentService documentService;

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

            SSSClient sssClient = new SSSClient();
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
    public Callable getDisscussionList(@PathVariable String fileEntityId) {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            OIDCAuthenticationToken token = (OIDCAuthenticationToken) auth;
            SSSClient sssClient = new SSSClient();
            String downloadLink = sssClient.getSssServerAddress() +
                    "/files/files/download?" +
                    "file=" + fileEntityId + "&key=" + token.getAccessTokenValue();
            return new ResponseEntity<>(downloadLink, HttpStatus.OK);
        };
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
