package de.hska.ld.content.service.impl;

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.DocumentEtherpadInfo;
import de.hska.ld.content.persistence.repository.DocumentEtherpadInfoRepository;
import de.hska.ld.content.service.DocumentEtherpadInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentEtherpadInfoServiceImpl implements DocumentEtherpadInfoService {

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
        documentEtherpadInfo.setGroupPadId(readOnlyId);
        documentEtherpadInfoRepository.save(documentEtherpadInfo);
    }

    @Override
    @Transactional(readOnly = false)
    public void storeGroupPadIdForDocument(String groupPadId, Document document) {
        // associate GroupPadId for the Document and save it
        DocumentEtherpadInfo documentEtherpadInfo = new DocumentEtherpadInfo();
        documentEtherpadInfo.setGroupPadId(groupPadId);
        documentEtherpadInfo.setDocument(document);
        documentEtherpadInfoRepository.save(documentEtherpadInfo);
    }
}
