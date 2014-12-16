package de.hska.ld.content.service.impl;

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
    @Transactional(readOnly = true)
    public DocumentEtherpadInfo save(DocumentEtherpadInfo documentEtherpadInfo) {
        return documentEtherpadInfoRepository.save(documentEtherpadInfo);
    }
}
