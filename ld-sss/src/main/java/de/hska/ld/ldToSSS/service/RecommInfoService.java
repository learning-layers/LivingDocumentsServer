package de.hska.ld.ldToSSS.service;

import de.hska.ld.ldToSSS.persistence.domain.RecommInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

public interface RecommInfoService {

     RecommInfo save(RecommInfo recommInfo);

     //void add(RecommInfo recommInfo);
     //RecommInfo getInsertionID();

     //List<RecommInfo> findAll();

     Page<RecommInfo> findAll(Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty);

     RecommInfo findByEntity(String entity);

     RecommInfo findOneUser(Long id);

     RecommInfo findOneFile(Long id);

     RecommInfo updateUser(Long id, RecommInfo recommInfo);

     RecommInfo updateFile(Long id, RecommInfo recommInfo);

     void deleteUser(Long id);

     void deleteFile(Long id);

}


