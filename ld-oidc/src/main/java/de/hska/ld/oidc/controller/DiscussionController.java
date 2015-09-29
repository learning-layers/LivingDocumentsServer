package de.hska.ld.oidc.controller;

import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.util.Core;
import de.hska.ld.oidc.client.SSSClient;
import de.hska.ld.oidc.dto.SSSDiscsDto;
import de.hska.ld.oidc.dto.SSSEntityDto;
import de.hska.ld.oidc.dto.SSSFileEntitiesDto;
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

            sssDiscsDto.getDiscs().stream().forEach(disc -> {
                disc.getAttachedEntities().forEach(attachedEntity -> {
                    if (!("placeholder".equals(attachedEntity.getType()) && "evernoteNotebook".equals(attachedEntity.getType()))) {
                        if ("evernoteResource".equals(attachedEntity.getType()) || "evernoteNote".equals(attachedEntity.getType())) {
                            constructFileDownloadURI(sssClient, attachedEntity, token);
                        }
                    }
                });
                disc.getEntries().stream().forEach(entry -> {

                });
            });

            return new ResponseEntity<>(sssDiscsDto.getDiscs(), HttpStatus.OK);
        };
    }

    private SSSFileEntitiesDto constructFileDownloadURI(SSSClient sssClient, SSSEntityDto attachedEntity, OIDCAuthenticationToken token) {
        SSSFileEntitiesDto fileEntitiesDto = null;
        try {
            fileEntitiesDto = sssClient.getFileEntity(attachedEntity.getId(), token.getAccessTokenValue());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileEntitiesDto;
    }
}
