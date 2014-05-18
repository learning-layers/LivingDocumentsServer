package de.hska.livingdocuments.core.persistence.repository;

import de.hska.livingdocuments.core.persistence.domain.Subscription;
import org.springframework.data.repository.CrudRepository;

public interface SubscriptionRepository extends CrudRepository<Subscription, Long> {
}
