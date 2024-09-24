package dbp.exploreconnet.notification.infrastructure;

import dbp.exploreconnet.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
