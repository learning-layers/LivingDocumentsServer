package de.hska.ld.oidc.listeners;

import de.hska.ld.content.events.document.DocumentCreationEvent;
import de.hska.ld.content.events.document.DocumentReadEvent;
import de.hska.ld.content.persistence.domain.Access;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.oidc.client.SSSClient;
import de.hska.ld.oidc.client.exception.AuthenticationNotValidException;
import de.hska.ld.oidc.client.exception.CreationFailedException;
import de.hska.ld.oidc.client.exception.NotYetKnownException;
import de.hska.ld.oidc.dto.SSSLivingDocResponseDto;
import de.hska.ld.oidc.dto.SSSLivingdoc;
import de.hska.ld.oidc.dto.SSSLivingdocsResponseDto;
import de.hska.ld.oidc.dto.SSSUserDto;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class LDToSSSEventListener {

    @Autowired
    private SSSClient sssClient;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public void handleDocumentReadEvent(DocumentReadEvent event) throws IOException, CreationFailedException {
        Document document = (Document) event.getSource();
        System.out.println("LDToSSSEventListener: Reading document=" + document.getId() + ", title=" + document.getTitle());
        document = createAndShareLDocWithSSSUsers(document, "READ");
        event.setResultDocument(document);
    }

    @EventListener
    public void handleDocumentCreationEvent(DocumentCreationEvent event) throws IOException, CreationFailedException {
        Document newDocument = (Document) event.getSource();
        System.out.println("LDToSSSEventListener: Creating document=" + newDocument.getId() + ", title=" + newDocument.getTitle());
        newDocument = createAndShareLDocWithSSSUsers(newDocument, "WRITE");
        event.setResultDocument(newDocument);
    }

    private Document createAndShareLDocWithSSSUsers(Document document, String cmd) throws IOException, CreationFailedException {
        // Create the document as well in the SSS
        List<Access> accessList = document.getAccessList();
        List<String> emailAddressesThatHaveAccess = new ArrayList<>();
        for (Access access : accessList) {
            emailAddressesThatHaveAccess.add(access.getUser().getEmail());
        }
        // check if the living document is already known to the SSS
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OIDCAuthenticationToken token = (OIDCAuthenticationToken) auth;
        SSSLivingdoc sssLivingdoc = null;
        boolean isAlreadyKnownToSSS = false;
        String sssLivingDocId = null;
        Long newDocumentId = document.getId();
        try {
            try {
                SSSLivingDocResponseDto sssLivingdocsResponseDto = sssClient.getLDocById(newDocumentId, token.getAccessTokenValue());
                SSSLivingdoc sssLivingDoc = sssLivingdocsResponseDto.getLivingDoc();
                if (sssLivingDoc != null && sssLivingDoc.getId() != null) {
                    isAlreadyKnownToSSS = true;
                }
            } catch (NotYetKnownException e) {
                //
            }
            if (!isAlreadyKnownToSSS) {
                // create the living document in the SSS
                SSSLivingdocsResponseDto sssLivingdocsResponseDto2 = sssClient.createDocument(document, null, token.getAccessTokenValue());
                sssLivingDocId = sssLivingdocsResponseDto2.getLivingDoc();
                if (sssLivingDocId == null) {
                    throw new CreationFailedException(newDocumentId);
                }
            }
            // Retrieve users/emails that have access to this living document declared by the SSS
            SSSLivingDocResponseDto sssLivingdocsResponseDto = sssClient.getLDocEmailsById(newDocumentId, token.getAccessTokenValue());
            SSSLivingdoc sssLivingDoc = sssLivingdocsResponseDto.getLivingDoc();
            if (sssLivingDoc != null && sssLivingDoc.getUsers() != null) {
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (SSSUserDto userDto : sssLivingDoc.getUsers()) {
                    if (!emailAddressesThatHaveAccess.contains(userDto.getLabel())) {
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
                if (!"".equals(userIds)) {
                    Document resultDocument = documentService.addAccess(document.getId(), userIds, "READ;WRITE");
                    resultDocument.getAttachmentList().size();
                    return resultDocument;
                }
            }
        } catch (AuthenticationNotValidException eAuth) {
            //
        }
        return document;
    }
}