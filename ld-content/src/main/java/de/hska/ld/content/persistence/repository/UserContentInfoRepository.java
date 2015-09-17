package de.hska.ld.content.persistence.repository;

import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.persistence.domain.UserContentInfo;
import de.hska.ld.core.persistence.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserContentInfoRepository extends CrudRepository<UserContentInfo, Long> {
    UserContentInfo findByUser(User user);

    UserContentInfo findById(Long id);

    @Query("SELECT uctl FROM UserContentInfo uc LEFT JOIN uc.tagList uctl WHERE uc.id = :userContentId")
    Page<Tag> findAllTagsForUserContent(@Param("userContentId") Long userContentId, Pageable pageable);
}