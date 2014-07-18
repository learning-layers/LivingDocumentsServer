package de.hska.ld.content.persistence.repository;

import de.hska.ld.content.persistence.domain.Subscription;
import org.springframework.data.repository.CrudRepository;

public interface TagRepository extends CrudRepository<Subscription, Long> {
}
