package de.hska.ld.ldToSSS.service.impl;

import de.hska.ld.content.service.DocumentService;
import de.hska.ld.ldToSSS.persistence.domain.RecommInfo;
import de.hska.ld.ldToSSS.service.RecommInfoService;
import de.hska.ld.ldToSSS.persistence.repository.RecommInfoRepository;
import de.hska.ld.core.persistence.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommInfoServiceImpl implements RecommInfoService {

    @Autowired
    private RecommInfoRepository recommInfoRepository;

    @Override
    @Transactional(readOnly = true)
    public RecommInfo save(RecommInfo recommInfo) {
        return recommInfoRepository.save(recommInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public RecommInfo findOne(Long id) {
        return recommInfoRepository.findOne(id);
    }


}
