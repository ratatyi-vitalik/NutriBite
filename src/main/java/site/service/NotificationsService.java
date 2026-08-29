package site.service;

import site.persistent.entity.Notification;
import site.persistent.entity.User;
import site.persistent.repository.NotificationRepository;

public class NotificationsService {
    NotificationRepository notificationRepository;

    public NotificationsService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void sendNotification(User user, String title, String message, String link) {
        notificationRepository.save(new Notification(user, title, message, link));
    }
}
