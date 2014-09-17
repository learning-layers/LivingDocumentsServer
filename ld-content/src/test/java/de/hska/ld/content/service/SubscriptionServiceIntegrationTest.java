package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Notification;
import de.hska.ld.content.persistence.domain.Subscription;
import de.hska.ld.core.AbstractIntegrationTest;
import de.hska.ld.core.service.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static de.hska.ld.content.ContentFixture.newDocument;

public class SubscriptionServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    DocumentService documentService;

    @Autowired
    SubscriptionService subscriptionService;

    @Autowired
    UserService userService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        setAuthentication(testUser);
    }

    @Test
    public void testThatNotificationIsDelivered() {
        Document document = documentService.save(newDocument());

        // create subscription for main content
        document = documentService.addSubscription(document.getId(), Subscription.Type.MAIN_CONTENT);

        // make a change to the document and therefore trigger the notification creation process
        document.setTitle(document.getTitle() + "(updated 1)");
        document = documentService.save(document);
        document.setTitle(document.getTitle() + "(updated 2)");
        document = documentService.save(document);
        document.setTitle(document.getTitle() + "(updated 3)");
        document = documentService.save(document);
        document.setTitle(document.getTitle() + "(updated 4)");
        document = documentService.save(document);

        // retrieve notifications
        List<Notification> notificationList = subscriptionService.getNotifications();
        Assert.assertNotNull(notificationList);
        Assert.assertTrue(notificationList.size() == 4);

        List<Notification> toBeMarkedAsReadNotificationList = new ArrayList<>();
        toBeMarkedAsReadNotificationList.add(notificationList.get(0));
        toBeMarkedAsReadNotificationList.add(notificationList.get(1));
        subscriptionService.markNotificationsAsRead(toBeMarkedAsReadNotificationList);

        List<Notification> notificationListAfterRead = subscriptionService.getNotifications();
        Assert.assertNotNull(notificationListAfterRead);
        long notReadAmount = notificationListAfterRead.stream().filter(n -> !n.isMarkedAsRead()).count();
        long readAmount = notificationListAfterRead.stream().filter(Notification::isMarkedAsRead).count();
        Assert.assertEquals(2L, notReadAmount);
        Assert.assertEquals(2L, readAmount);

        /*for (Notification notification : notificationList) {
            Assert.assertTrue(notification.getDocumentId().equals(document.getId()));
        }

        // retrieve notifications after the notification has been delivered
        notificationList = subscriptionService.getNotifications();
        Assert.assertTrue(notificationList.size() == 0);*/
    }
}
