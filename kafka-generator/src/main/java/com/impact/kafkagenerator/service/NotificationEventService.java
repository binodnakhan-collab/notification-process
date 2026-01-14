package com.impact.kafkagenerator.service;

import com.impact.kafkagenerator.payload.NotificationEvent;

public interface NotificationEventService {

    void sendNotificationEvent(NotificationEvent notificationEvent);
}
