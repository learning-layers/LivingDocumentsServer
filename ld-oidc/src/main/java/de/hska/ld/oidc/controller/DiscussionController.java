package de.hska.ld.oidc.controller;

import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.util.Core;
import de.hska.ld.oidc.client.SSSClient;
import de.hska.ld.oidc.dto.*;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping(Core.RESOURCE_USER + "/disussions")
public class DiscussionController {

    @Autowired
    private DocumentService documentService;

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
            SSSFileEntitiesDto sssFileEntitiesDto = constructFileDownloadURI(sssClient, preloadAttachmentIds, token);
            DiscussionListDto discussionListDto = new DiscussionListDto();
            discussionListDto.setDiscussions(sssDiscsDto);
            discussionListDto.setFileEnities(sssFileEntitiesDto);
            return new ResponseEntity<>(discussionListDto, HttpStatus.OK);
        };
    }

    private SSSFileEntitiesDto constructFileDownloadURI(SSSClient sssClient, List<String> attachmentIds, OIDCAuthenticationToken token) {
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
}
