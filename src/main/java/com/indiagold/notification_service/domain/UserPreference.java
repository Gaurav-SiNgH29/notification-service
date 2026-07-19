package com.indiagold.notification_service.domain;

import com.indiagold.notification_service.domain.enums.ChannelType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;


@Entity
@Table(
    name = "user_preferences",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "channel"})
    }
)
@Getter
@Setter
@NoArgsConstructor
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChannelType channel;

    @Column(name = "opted_in", nullable = false)
    private boolean optedIn;
}