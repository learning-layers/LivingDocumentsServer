package de.hska.ld.etherpad.service;

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.etherpad.persistence.domain.DocumentEtherpadInfo;

public interface DocumentEtherpadInfoService {

    DocumentEtherpadInfo save(DocumentEtherpadInfo documentEtherpadInfo);

    DocumentEtherpadInfo findByDocument(Document document);

    String getReadOnlyIdForDocument(Document document);

    void storeReadOnlyIdForDocument(String readOnlyId, Document document);

    void storeGroupPadIdForDocument(String groupPadId, Document document);

    String getGroupPadIdForDocument(Document document);

    DocumentEtherpadInfo findByGroupPadId(String padId);
}


