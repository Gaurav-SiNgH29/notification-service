package com.indiagold.notification_service.repository;

import com.indiagold.notification_service.domain.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {

}