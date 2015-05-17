package de.hska.ld.ldToSSS.service;

import de.hska.ld.ldToSSS.persistence.domain.RecommInfo;

public interface RecommInfoService {

     RecommInfo save(RecommInfo recommInfo);

     RecommInfo findOne(Long id);

}


