package com.indiagold.notification_service.domain;

import com.indiagold.notification_service.domain.enums.ChannelType;
import com.indiagold.notification_service.domain.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_history")
@Getter
@Setter
@NoArgsConstructor
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChannelType channel;

    // @Column(nullable = false, length = 255)
    // private String title;

    // @Column(nullable = false, columnDefinition = "TEXT")
    // private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "dispatched_at", nullable = false, updatable = false)
    private LocalDateTime dispatchedAt;

    @PrePersist
    protected void onCreate() {
        this.dispatchedAt = LocalDateTime.now();
    }
}
