package de.hska.livingdocuments.core.service.impl;

import de.hska.livingdocuments.core.persistence.domain.Subscription;
import de.hska.livingdocuments.core.persistence.repository.SubscriptionRepository;
import de.hska.livingdocuments.core.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Martin on 18.05.2014.
 */
public class SubscriptionServiceImpl implements SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Override
    public Subscription save(Subscription subscription) {
        return subscriptionRepository.save(subscription);
    }
}
