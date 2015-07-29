package de.hska.ld.etherpad.persistence.repository;

import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.etherpad.persistence.domain.UserEtherpadInfo;
import org.springframework.data.repository.CrudRepository;

public interface UserEtherpadInfoRepository extends CrudRepository<UserEtherpadInfo, Long> {
    UserEtherpadInfo findByUser(User user);

    UserEtherpadInfo findById(Long id);

    UserEtherpadInfo findByAuthorId(String authorId);
}
