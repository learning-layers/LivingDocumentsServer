package de.hska.ld.etherpad.service.impl;

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.etherpad.persistence.domain.DocumentEtherpadInfo;
import de.hska.ld.etherpad.persistence.repository.DocumentEtherpadInfoRepository;
import de.hska.ld.etherpad.service.DocumentEtherpadInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentEtherpadInfoServiceImpl implements DocumentEtherpadInfoService {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentEtherpadInfoRepository documentEtherpadInfoRepository;

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

}
