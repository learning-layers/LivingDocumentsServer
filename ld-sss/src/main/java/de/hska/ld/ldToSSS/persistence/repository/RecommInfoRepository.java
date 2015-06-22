package de.hska.ld.ldToSSS.persistence.repository;

import de.hska.ld.ldToSSS.persistence.domain.RecommInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;

public interface RecommInfoRepository extends CrudRepository<RecommInfo, Long> {
    Page<RecommInfo> findAll(Pageable pageable);
}

