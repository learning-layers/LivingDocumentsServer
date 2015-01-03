package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.DocumentEtherpadInfo;

public interface DocumentEtherpadInfoService {

    public DocumentEtherpadInfo save(DocumentEtherpadInfo documentEtherpadInfo);

    public DocumentEtherpadInfo findByDocument(Document document);

    public String getReadOnlyIdForDocument(Document document);

    public void storeReadOnlyIdForDocument(String readOnlyId, Document document);

    public void storeGroupPadIdForDocument(String groupPadId, Document document);
}


