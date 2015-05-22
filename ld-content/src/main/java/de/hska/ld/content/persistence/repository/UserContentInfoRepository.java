package de.hska.ld.content.persistence.repository;

import de.hska.ld.content.persistence.domain.UserContentInfo;
import de.hska.ld.core.persistence.domain.User;
import org.springframework.data.repository.CrudRepository;

public interface UserContentInfoRepository extends CrudRepository<UserContentInfo, Long> {
    UserContentInfo findByUser(User user);

    UserContentInfo findById(Long id);
}
