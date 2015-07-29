package de.hska.ld.etherpad.persistence.repository;

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.etherpad.persistence.domain.DocumentEtherpadInfo;
import org.springframework.data.repository.CrudRepository;

public interface DocumentEtherpadInfoRepository extends CrudRepository<DocumentEtherpadInfo, Long> {

    DocumentEtherpadInfo findByDocument(Document document);

    DocumentEtherpadInfo findByGroupPadId(String padId);
}

