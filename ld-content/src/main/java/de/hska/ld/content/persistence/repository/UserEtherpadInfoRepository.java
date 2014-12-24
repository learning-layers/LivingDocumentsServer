package de.hska.ld.content.persistence.repository;

import de.hska.ld.content.persistence.domain.UserEtherpadInfo;
import de.hska.ld.core.persistence.domain.User;
import org.springframework.data.repository.CrudRepository;

public interface UserEtherpadInfoRepository extends CrudRepository<UserEtherpadInfo, Long> {
    UserEtherpadInfo findByUser(User user);
    UserEtherpadInfo findByAuthorId(String AuthorId);

    UserEtherpadInfo findById(Long id);
}
