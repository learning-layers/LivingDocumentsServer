package de.hska.ld.content.persistence.repository;

import de.hska.ld.content.persistence.domain.Notification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends CrudRepository<Notification, Long> {

    List<Notification> findByUserIdAndDelivered(@Param("userId") Long userId,
                                                @Param("delivered") boolean delivered);
}
