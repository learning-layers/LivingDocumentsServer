package de.hska.ld.etherpad.service.impl;

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.util.Core;
import de.hska.ld.etherpad.client.EtherpadClient;
import de.hska.ld.etherpad.persistence.domain.DocumentEtherpadInfo;
import de.hska.ld.etherpad.persistence.domain.UserEtherpadInfo;
import de.hska.ld.etherpad.persistence.repository.DocumentEtherpadInfoRepository;
import de.hska.ld.etherpad.service.DocumentEtherpadInfoService;
import de.hska.ld.etherpad.service.UserEtherpadInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.UUID;

@Service
public class DocumentEtherpadInfoServiceImpl implements DocumentEtherpadInfoService {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserEtherpadInfoService userEtherpadInfoService;

    @Autowired
    private DocumentEtherpadInfoRepository documentEtherpadInfoRepository;

    @Autowired
    private EtherpadClient etherpadClient;

    @Override
    @Transactional(readOnly = false)
    public DocumentEtherpadInfo save(DocumentEtherpadInfo documentEtherpadInfo) {
        return documentEtherpadInfoRepository.save(documentEtherpadInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentEtherpadInfo findByDocument(Document document) {
        return documentEtherpadInfoRepository.findByDocument(document);
    }

    @Override
    @Transactional(readOnly = true)
    public String getReadOnlyIdForDocument(Document document) {
        DocumentEtherpadInfo documentEtherpadInfo = documentEtherpadInfoRepository.findByDocument(document);
        if (documentEtherpadInfo == null) {
            return null;
        } else {
            return documentEtherpadInfo.getReadOnlyId();
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void storeReadOnlyIdForDocument(String readOnlyId, Document document) {
        DocumentEtherpadInfo documentEtherpadInfo = documentEtherpadInfoRepository.findByDocument(document);
        documentEtherpadInfo.setReadOnlyId(readOnlyId);
        documentEtherpadInfoRepository.save(documentEtherpadInfo);
    }

    @Override
    @Transactional(readOnly = false)
    public void storeGroupPadIdForDocument(String groupPadId, Document document) {
        // associate GroupPadId for the Document and save it
        DocumentEtherpadInfo documentEtherpadInfo = documentEtherpadInfoRepository.findByDocument(document);
        if (documentEtherpadInfo == null) {
            documentEtherpadInfo = new DocumentEtherpadInfo();
        }
        documentEtherpadInfo.setGroupPadId(groupPadId);
        documentEtherpadInfo.setDocument(document);
        documentEtherpadInfoRepository.save(documentEtherpadInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public String getGroupPadIdForDocument(Document document) {
        Document dbDocument = documentService.findById(document.getId());
        DocumentEtherpadInfo documentEtherpadInfo = documentEtherpadInfoRepository.findByDocument(dbDocument);
        if (documentEtherpadInfo == null) {
            return null;
        } else {
            return documentEtherpadInfo.getGroupPadId();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentEtherpadInfo findByGroupPadId(String padId) {
        return documentEtherpadInfoRepository.findByGroupPadId(padId);
    }

    @Override
    @Transactional(readOnly = true)
    public String getGroupPadContent(String groupPadId) {
        String padText = null;
        try {
            padText = etherpadClient.getGroupPadContent(groupPadId);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return padText;
    }

    @Override
    @Transactional(readOnly = true)
    public String createGroupPadWithContent(Document document, String padText) {
        // for the given User check whether there is an AuthorId registered in Etherpad
        UserEtherpadInfo firstUserEtherPadInfoCheck = userEtherpadInfoService.getUserEtherpadInfoForCurrentUser();
        String authorId = null;
        String groupPadId = null;
        if (firstUserEtherPadInfoCheck != null) {
            authorId = firstUserEtherPadInfoCheck.getAuthorId();
        }

        try {
            if (authorId == null) {

                // if there is no AuthorId present register an AuthorId for the current User
                authorId = etherpadClient.createAuthor(Core.currentUser().getFullName());

                userEtherpadInfoService.storeAuthorIdForCurrentUser(authorId);
            }
            String groupId = null;
            groupId = etherpadClient.createGroup();
            String groupPadTitle = UUID.randomUUID().toString();
            while (groupPadTitle.endsWith("%")) {
                groupPadTitle = groupPadTitle.substring(0, groupPadTitle.length() - 1);
            }
            groupPadId = etherpadClient.createGroupPad(groupId, groupPadTitle);
            etherpadClient.setGroupPadContent(groupPadId, padText);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return groupPadId;
    }
}
