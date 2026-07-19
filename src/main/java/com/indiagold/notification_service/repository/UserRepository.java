package com.indiagold.notification_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.indiagold.notification_service.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
}
