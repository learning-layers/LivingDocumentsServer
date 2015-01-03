package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.DocumentEtherpadInfo;

public interface DocumentEtherpadInfoService {

    public DocumentEtherpadInfo save(DocumentEtherpadInfo documentEtherpadInfo);

    public DocumentEtherpadInfo findByDocument(Document document);

    public String getReadOnlyIdForDocument(Document document);

}


