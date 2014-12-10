package de.hska.ld.content.persistence.repository;

import de.hska.ld.content.persistence.domain.UserEtherpadInfo;
import de.hska.ld.core.persistence.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserEtherpadInfoRepository extends CrudRepository<UserEtherpadInfo, Long> {


    @Query("SELECT uei FROM UserEtherpadInfo uei WHERE uei.user = :user ")
    UserEtherpadInfo getEtherpadInfoByUser(@Param("user") User user);
}
