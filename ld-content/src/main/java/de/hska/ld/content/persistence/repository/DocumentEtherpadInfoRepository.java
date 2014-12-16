package de.hska.ld.content.persistence.repository;

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.DocumentEtherpadInfo;
import org.springframework.data.repository.CrudRepository;

public interface DocumentEtherpadInfoRepository extends CrudRepository<DocumentEtherpadInfo, Long> {
    DocumentEtherpadInfo findByDocument(Document document);
}

