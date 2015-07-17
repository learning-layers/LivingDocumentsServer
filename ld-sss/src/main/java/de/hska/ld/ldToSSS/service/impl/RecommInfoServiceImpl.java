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

import java.util.ArrayList;
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
/*
    @Override
    @Transactional(readOnly = false)
    public void add(RecommInfo recommInfo) {
        recommInfoRepository.add(recommInfo);
        return;
    }*/
//    @Override
//    @Transactional(readOnly = true)
//    public RecommInfo getInsertionID() {
//
//        return recommInfoRepository.findIDToInsert();
//    }
    @Override
    @Transactional(readOnly = true)
    public RecommInfo findByEntity(String entity) {
        return recommInfoRepository.findByEntity(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public RecommInfo findOneUser(Long id) {
        return recommInfoRepository.findByID(id, "USER");
    }

    @Override
    @Transactional(readOnly = true)
    public RecommInfo findOneFile(Long id) {
        return recommInfoRepository.findByID(id,"FILE");
    }

    /**
     * Update method for user recommendations
     * @param id Id of the user
     * @param recommInfo new recommendation info for this user
     * @return returns updated user recommendation info
     */
    @Override
    @Transactional(readOnly = false)
    public RecommInfo updateUser(Long id, RecommInfo recommInfo) {
        RecommInfo dbRecommInfo = findOneUser(id);
        if(dbRecommInfo!=null){
            dbRecommInfo.setRealm(recommInfo.getRealm());
            dbRecommInfo.setType(recommInfo.getType());
            dbRecommInfo.setEntity(recommInfo.getEntity());
            recommInfoRepository.save(dbRecommInfo);
            return recommInfo;
        }else{
            return null;
        }
    }

    /**
     * Update method for file recommendations
     * @param id Id of the file
     * @param recommInfo new recommendation info for this user
     * @return returns updated file recommendation info
     */
    @Override
    @Transactional(readOnly = false)
    public RecommInfo updateFile(Long id, RecommInfo recommInfo) {
        RecommInfo dbRecommInfo = findOneFile(id);
        if(dbRecommInfo!=null){
            dbRecommInfo.setRealm(recommInfo.getRealm());
            dbRecommInfo.setType(recommInfo.getType());
            dbRecommInfo.setEntity(recommInfo.getEntity());
            recommInfoRepository.save(dbRecommInfo);
            return recommInfo;
        }else{
            return null;
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteUser(Long id) {
        RecommInfo dbRecommInfo = findOneUser(id);
        if(dbRecommInfo!=null){
            //We just set up the column ("deleted") of the recommendation to 1
            dbRecommInfo.setDeleted(new Byte("1"));
            recommInfoRepository.save(dbRecommInfo);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteFile(Long id) {
        RecommInfo dbRecommInfo = findOneFile(id);
        if(dbRecommInfo!=null){
            //We just set up the column ("deleted") of the recommendation to 1
            dbRecommInfo.setDeleted(new Byte("1"));
            recommInfoRepository.save(dbRecommInfo);
        }
    }

    /*@Override
    @Transactional(readOnly = true)
    public List<RecommInfo> findAll() {
        return (List)recommInfoRepository.findAll();
    }*/

    @Override
    @Transactional(readOnly = true)
    public Page<RecommInfo> findAll(Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty) {
        Sort.Direction direction;
        if (Sort.Direction.ASC.toString().equals(sortDirection)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }
        Pageable pageable = new PageRequest(pageNumber, pageSize, direction, sortProperty);
        return recommInfoRepository.findAll(pageable);
    }



}
