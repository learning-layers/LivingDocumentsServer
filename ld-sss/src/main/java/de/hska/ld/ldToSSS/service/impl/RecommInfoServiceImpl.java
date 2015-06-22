package de.hska.ld.ldToSSS.service.impl;

import de.hska.ld.ldToSSS.persistence.domain.RecommInfo;
import de.hska.ld.ldToSSS.persistence.repository.RecommInfoRepository;
import de.hska.ld.ldToSSS.service.RecommInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecommInfoServiceImpl implements RecommInfoService {

    @Autowired
    private RecommInfoRepository recommInfoRepository;

    @Override
    @Transactional(readOnly = false)
    public RecommInfo save(RecommInfo recommInfo) {
        return recommInfoRepository.save(recommInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public RecommInfo findOne(Long id) {
        return recommInfoRepository.findOne(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecommInfo> getRecommInfoPage(Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty) {
        Sort.Direction direction;
        if (Sort.Direction.ASC.toString().equals(sortDirection)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }
        Pageable pageable = new PageRequest(pageNumber, pageSize, direction, sortProperty);
        return recommInfoRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = false)
    public RecommInfo update(Long id, RecommInfo recommInfo) {
        RecommInfo dbRecommInfo = findOne(id);
        if(dbRecommInfo!=null){
            dbRecommInfo.setRealm(recommInfo.getRealm());
            dbRecommInfo.setForUser(recommInfo.getForUser());
            dbRecommInfo.setEntity(recommInfo.getEntity());
            return recommInfo;
        }else{
            return null;
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(Long id) {
        RecommInfo dbRecommInfo = findOne(id);
        if(dbRecommInfo!=null){
            //We just set up the column ("deleted") of the recommendation to 1
            dbRecommInfo.setDeleted(new Byte("1"));
        }
    }


}
