package de.hska.ld.ldToSSS.service;

import de.hska.ld.ldToSSS.persistence.domain.RecommInfo;
import org.springframework.data.domain.Page;

public interface RecommInfoService{

     RecommInfo save(RecommInfo recommInfo);

     RecommInfo findOne(Long id);

     RecommInfo update(Long id, RecommInfo recommInfo);

     Page<RecommInfo> getRecommInfoPage(Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty);

     void delete(Long id);
}


