/**
 * Code contributed to the Learning Layers project
 * http://www.learning-layers.eu
 * Development is partly funded by the FP7 Programme of the European
 * Commission under Grant Agreement FP7-ICT-318209.
 * Copyright (c) 2014, Karlsruhe University of Applied Sciences.
 * For a list of contributors see the AUTHORS file at the top-level directory
 * of this distribution.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hska.ld.content.service.impl;

import de.hska.ld.content.persistence.domain.Notification;
import de.hska.ld.content.persistence.domain.Subscription;
import de.hska.ld.content.persistence.repository.NotificationRepository;
import de.hska.ld.content.persistence.repository.SubscriptionRepository;
import de.hska.ld.content.service.SubscriptionService;
import de.hska.ld.core.persistence.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class SubscriptionServiceImpl implements SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public Subscription save(Subscription subscription) {
        return subscriptionRepository.save(subscription);
    }

    @Override
    public void saveNotification(Long documentId, Long subscriberId, Long editorId, Subscription.Type type) {
        Notification notification = new Notification();
        notification.setDocumentId(documentId);
        notification.setSubscriberId(subscriberId);
        notification.setEditorId(editorId);
        notification.setType(type);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public List<Notification> deliverNotifications(User user) {
        List<Notification> subscriptionItemsList = notificationRepository
                .findBySubscriberIdAndDelivered(user.getId(), false);

        for (Notification notification : subscriptionItemsList) {
            notification.setDelivered(true);
            notificationRepository.save(notification);
        }

        return subscriptionItemsList;
    }
}
