package com.indiagold.notification_service.service;

import com.indiagold.notification_service.channel.DispatchResult;
import com.indiagold.notification_service.channel.NotificationChannel;
import com.indiagold.notification_service.domain.NotificationHistory;
import com.indiagold.notification_service.domain.User;
import com.indiagold.notification_service.domain.UserPreference;
import com.indiagold.notification_service.domain.enums.ChannelType;
import com.indiagold.notification_service.domain.enums.DeliveryStatus;
import com.indiagold.notification_service.dto.ChannelResult;
import com.indiagold.notification_service.dto.NotificationRequest;
import com.indiagold.notification_service.exception.UserNotFoundException;
import com.indiagold.notification_service.repository.NotificationHistoryRepository;
import com.indiagold.notification_service.repository.UserPreferenceRepository;
import com.indiagold.notification_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final Map<ChannelType, NotificationChannel> channelProviders;

    public NotificationService(
            UserRepository userRepository,
            UserPreferenceRepository userPreferenceRepository,
            NotificationHistoryRepository notificationHistoryRepository,
            List<NotificationChannel> channels) {

        this.userRepository = userRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.notificationHistoryRepository = notificationHistoryRepository;
        this.channelProviders = channels.stream()
                .collect(Collectors.toMap(
                        NotificationChannel::getChannelType,
                        channel -> channel
                ));
    }

    public List<ChannelResult> processNotification(NotificationRequest request) {
        log.info("Processing notification for userId={}, requestedChannels={}",
                request.getUserId(), request.getChannels());

        User user = findUserOrThrow(request.getUserId());
        Set<ChannelType> optedInChannels = fetchOptedInChannels(user.getId());

        log.info("User userId={} is opted into channels={}", user.getId(), optedInChannels);

        List<ChannelResult> results = new ArrayList<>();

        for (ChannelType requestedChannel : request.getChannels()) {
            ChannelResult result = processChannel(
                    user,
                    requestedChannel,
                    optedInChannels,
                    request.getTitle(),
                    request.getBody()
            );
            results.add(result);
        }

        log.info("Notification processing complete for userId={}, results={}",
                user.getId(), results);

        return results;
    }

    // ─── Private Helpers

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for userId={}", userId);
                    return new UserNotFoundException(userId);
                });
    }

    private Set<ChannelType> fetchOptedInChannels(Long userId) {
        return userPreferenceRepository.findByUserId(userId)
                .stream()
                .filter(UserPreference::isOptedIn)
                .map(UserPreference::getChannel)
                .collect(Collectors.toSet());
    }

    private ChannelResult processChannel(
            User user,
            ChannelType requestedChannel,
            Set<ChannelType> optedInChannels,
            String title,
            String body) {

        if (!optedInChannels.contains(requestedChannel)) {
            log.info("Channel {} SKIPPED for userId={} — not opted in",
                    requestedChannel, user.getId());
            saveHistory(user, requestedChannel,
                    DeliveryStatus.SKIPPED, null);
            return new ChannelResult(requestedChannel, DeliveryStatus.SKIPPED);
        }

        NotificationChannel provider = channelProviders.get(requestedChannel);
        DispatchResult dispatchResult = provider.send(user, title, body);

        saveHistory(user, requestedChannel,
                dispatchResult.getStatus(), dispatchResult.getErrorMessage());

        return new ChannelResult(requestedChannel, dispatchResult.getStatus());
    }

    private void saveHistory(
            User user,
            ChannelType channel,
            DeliveryStatus status,
            String errorMessage) {

        NotificationHistory history = new NotificationHistory();
        history.setUser(user);
        history.setChannel(channel);
        history.setStatus(status);
        history.setErrorMessage(errorMessage);

        notificationHistoryRepository.save(history);

        log.info("History saved — userId={}, channel={}, status={}",
                user.getId(), channel, status);
    }
}