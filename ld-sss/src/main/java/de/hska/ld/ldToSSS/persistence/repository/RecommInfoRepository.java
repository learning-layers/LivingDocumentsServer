package de.hska.ld.ldToSSS.persistence.repository;

import de.hska.ld.ldToSSS.persistence.domain.RecommInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface RecommInfoRepository extends CrudRepository<RecommInfo,Long>{

    //Page<RecommInfo> findAll(Pageable pageable);

    @Query("SELECT t FROM RecommInfo t WHERE t.typeID = :typeID AND t.type = :entityType")
    RecommInfo findByID(@Param("typeID") Long typeID, @Param("entityType") String entityType);

    @Query("SELECT t FROM RecommInfo t WHERE t.type = :objectType")
    RecommInfo findByType( @Param("objectType") String objectType);

}



