package com.indiagold.notification_service;

import com.indiagold.notification_service.channel.DispatchResult;
import com.indiagold.notification_service.channel.NotificationChannel;
import com.indiagold.notification_service.domain.Notification;
import com.indiagold.notification_service.domain.User;
import com.indiagold.notification_service.domain.UserPreference;
import com.indiagold.notification_service.domain.enums.ChannelType;
import com.indiagold.notification_service.domain.enums.DeliveryStatus;
import com.indiagold.notification_service.dto.ChannelResult;
import com.indiagold.notification_service.dto.NotificationRequest;
import com.indiagold.notification_service.exception.UserNotFoundException;
import com.indiagold.notification_service.repository.NotificationHistoryRepository;
import com.indiagold.notification_service.repository.NotificationRepository;
import com.indiagold.notification_service.repository.UserPreferenceRepository;
import com.indiagold.notification_service.service.NotificationService;
import com.indiagold.notification_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    // ─── Mocks ──────────────────────────────────────────────────────
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @Mock
    private NotificationHistoryRepository notificationHistoryRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationChannel emailChannel;

    @Mock
    private NotificationChannel smsChannel;

    // ─── System Under Test ──────────────────────────────────────────
    private NotificationService notificationService;

    // ─── Test Data ──────────────────────────────────────────────────
    private User testUser;
    private Notification savedNotification;

    @BeforeEach
    void setUp() {
        when(emailChannel.getChannelType()).thenReturn(ChannelType.EMAIL);
        when(smsChannel.getChannelType()).thenReturn(ChannelType.SMS);

        notificationService = new NotificationService(
                userRepository,
                userPreferenceRepository,
                notificationHistoryRepository,
                notificationRepository,
                List.of(emailChannel, smsChannel)
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Alice");
        testUser.setEmail("alice@example.com");
        testUser.setPhone("+919999911111");
        testUser.setDeviceToken("device-token-alice");

        // Every test that reaches channel processing needs a saved notification
        // returned from the repository mock
        savedNotification = new Notification();
        savedNotification.setId(1L);
        savedNotification.setUser(testUser);
        savedNotification.setTitle("Test Title");
        savedNotification.setBody("Test Body");
    }

    // ─── Test 1 — User Not Found ─────────────────────────────────────
    @Test
    @DisplayName("Should throw UserNotFoundException when userId does not exist")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotificationRequest request = buildRequest(99L, List.of(ChannelType.EMAIL));

        assertThatThrownBy(() -> notificationService.processNotification(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ─── Test 2 — Channel Skipped When Not Opted In ──────────────────
    @Test
    @DisplayName("Should skip channel when user has not opted in")
    void shouldSkipChannelWhenNotOptedIn() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userPreferenceRepository.findByUserId(1L))
                .thenReturn(List.of(
                        buildPreference(ChannelType.EMAIL, true)
                ));
        when(notificationRepository.save(any())).thenReturn(savedNotification);
        when(emailChannel.send(any(), any(), any()))
                .thenReturn(DispatchResult.delivered());

        NotificationRequest request = buildRequest(1L,
                List.of(ChannelType.EMAIL, ChannelType.SMS));

        List<ChannelResult> results = notificationService.processNotification(request);

        assertThat(results).hasSize(2);
        assertThat(results)
                .filteredOn(r -> r.getChannel() == ChannelType.SMS)
                .extracting(ChannelResult::getStatus)
                .containsExactly(DeliveryStatus.SKIPPED);

        verify(smsChannel, never()).send(any(), any(), any());
    }

    // ─── Test 3 — Channel Dispatched When Opted In ───────────────────
    @Test
    @DisplayName("Should dispatch to channel when user has opted in")
    void shouldDispatchWhenUserOptedIn() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userPreferenceRepository.findByUserId(1L))
                .thenReturn(List.of(
                        buildPreference(ChannelType.EMAIL, true)
                ));
        when(notificationRepository.save(any())).thenReturn(savedNotification);
        when(emailChannel.send(any(), any(), any()))
                .thenReturn(DispatchResult.delivered());

        NotificationRequest request = buildRequest(1L, List.of(ChannelType.EMAIL));

        List<ChannelResult> results = notificationService.processNotification(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        verify(emailChannel, times(1)).send(any(), any(), any());
    }

    // ─── Test 4 — All Channels Skipped ───────────────────────────────
    @Test
    @DisplayName("Should return all SKIPPED when user opted out of everything")
    void shouldReturnAllSkippedWhenUserOptedOutOfEverything() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userPreferenceRepository.findByUserId(1L))
                .thenReturn(List.of(
                        buildPreference(ChannelType.EMAIL, false),
                        buildPreference(ChannelType.SMS, false)
                ));
        when(notificationRepository.save(any())).thenReturn(savedNotification);

        NotificationRequest request = buildRequest(1L,
                List.of(ChannelType.EMAIL, ChannelType.SMS));

        List<ChannelResult> results = notificationService.processNotification(request);

        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(ChannelResult::getStatus)
                .containsOnly(DeliveryStatus.SKIPPED);

        verify(emailChannel, never()).send(any(), any(), any());
        verify(smsChannel, never()).send(any(), any(), any());
    }

    // ─── Test 5 — Partial Skip and Dispatch ──────────────────────────
    @Test
    @DisplayName("Should respect intersection — dispatch opted-in, skip opted-out")
    void shouldRespectIntersectionOfRequestedAndOptedInChannels() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userPreferenceRepository.findByUserId(1L))
                .thenReturn(List.of(
                        buildPreference(ChannelType.EMAIL, true),
                        buildPreference(ChannelType.SMS, false)
                ));
        when(notificationRepository.save(any())).thenReturn(savedNotification);
        when(emailChannel.send(any(), any(), any()))
                .thenReturn(DispatchResult.delivered());

        NotificationRequest request = buildRequest(1L,
                List.of(ChannelType.EMAIL, ChannelType.SMS));

        List<ChannelResult> results = notificationService.processNotification(request);

        assertThat(results).hasSize(2);
        assertThat(results)
                .filteredOn(r -> r.getChannel() == ChannelType.EMAIL)
                .extracting(ChannelResult::getStatus)
                .containsExactly(DeliveryStatus.DELIVERED);
        assertThat(results)
                .filteredOn(r -> r.getChannel() == ChannelType.SMS)
                .extracting(ChannelResult::getStatus)
                .containsExactly(DeliveryStatus.SKIPPED);
    }

    // ─── Test 6 — Failed Dispatch Recorded ───────────────────────────
    @Test
    @DisplayName("Should record FAILED status when provider fails")
    void shouldRecordFailedStatusWhenProviderFails() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userPreferenceRepository.findByUserId(1L))
                .thenReturn(List.of(
                        buildPreference(ChannelType.EMAIL, true)
                ));
        when(notificationRepository.save(any())).thenReturn(savedNotification);
        when(emailChannel.send(any(), any(), any()))
                .thenReturn(DispatchResult.failed("Simulated failure"));

        NotificationRequest request = buildRequest(1L, List.of(ChannelType.EMAIL));

        List<ChannelResult> results = notificationService.processNotification(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(DeliveryStatus.FAILED);
    }

    // ─── Test 7 — History Saved For Every Outcome ────────────────────
    @Test
    @DisplayName("Should save history record for every channel outcome")
    void shouldSaveHistoryForEveryChannelOutcome() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userPreferenceRepository.findByUserId(1L))
                .thenReturn(List.of(
                        buildPreference(ChannelType.EMAIL, true),
                        buildPreference(ChannelType.SMS, false)
                ));
        when(notificationRepository.save(any())).thenReturn(savedNotification);
        when(emailChannel.send(any(), any(), any()))
                .thenReturn(DispatchResult.delivered());

        NotificationRequest request = buildRequest(1L,
                List.of(ChannelType.EMAIL, ChannelType.SMS));

        notificationService.processNotification(request);

        // One save for the notification itself
        verify(notificationRepository, times(1)).save(any());

        // Two saves for history — one per channel
        verify(notificationHistoryRepository, times(2)).save(any());
    }

    // ─── Test 8 — Notification Saved Before Dispatch ─────────────────
    @Test
    @DisplayName("Should save notification record before dispatching to channels")
    void shouldSaveNotificationBeforeDispatching() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userPreferenceRepository.findByUserId(1L))
                .thenReturn(List.of(
                        buildPreference(ChannelType.EMAIL, true)
                ));
        when(notificationRepository.save(any())).thenReturn(savedNotification);
        when(emailChannel.send(any(), any(), any()))
                .thenReturn(DispatchResult.delivered());

        NotificationRequest request = buildRequest(1L, List.of(ChannelType.EMAIL));

        notificationService.processNotification(request);

        // Notification must be saved exactly once
        verify(notificationRepository, times(1)).save(any());
    }

    // ─── Helpers ─────────────────────────────────────────────────────

    private NotificationRequest buildRequest(Long userId, List<ChannelType> channels) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setTitle("Test Title");
        request.setBody("Test Body");
        request.setChannels(channels);
        return request;
    }

    private UserPreference buildPreference(ChannelType channel, boolean optedIn) {
        UserPreference preference = new UserPreference();
        preference.setChannel(channel);
        preference.setOptedIn(optedIn);
        return preference;
    }
}