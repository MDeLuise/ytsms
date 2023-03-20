package com.github.mdeluise.ytsms.video;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.mdeluise.ytsms.authentication.User;
import com.github.mdeluise.ytsms.channel.ChannelService;
import com.github.mdeluise.ytsms.common.AuthenticatedUserService;
import com.github.mdeluise.ytsms.exception.ResourceNotFoundException;
import com.github.mdeluise.ytsms.exception.UnauthorizedException;
import com.github.mdeluise.ytsms.subscription.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class VideoService {
    private final VideoRepository videoRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final SubscriptionRepository subscriptionRepository;
    private final ChannelService channelService;
    private final Logger logger = LoggerFactory.getLogger(VideoService.class);


    @Autowired
    public VideoService(VideoRepository videoRepository, AuthenticatedUserService authenticatedUserService,
                        SubscriptionRepository subscriptionRepository, ChannelService channelService) {
        this.videoRepository = videoRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.subscriptionRepository = subscriptionRepository;
        this.channelService = channelService;
    }


    public Page<Video> getAll(Pageable pageable) {
        final User authenticatedUser = authenticatedUserService.getAuthenticatedUser();
        final Set<String> channelIdSubscribed =
            authenticatedUser.getSubscriptions().stream().map(sub -> sub.getChannel().getId())
                             .collect(Collectors.toSet());
        return videoRepository.findByChannelIdIn(channelIdSubscribed, pageable);
    }


    public Video save(Video toSave) {
        return videoRepository.save(toSave);
    }


    public void remove(Video video) {
        videoRepository.delete(video);
    }


    public boolean exists(String id) {
        return videoRepository.existsById(id);
    }


    public void removeAll() {
        videoRepository.findAll().forEach(this::remove);
    }


    public Page<Video> getAllByChannelIds(Pageable paging, List<String> channelIds) {
        final User authenticatedUser = authenticatedUserService.getAuthenticatedUser();
        channelIds.forEach(channelId -> {
            if (!subscriptionRepository.existsByUserAndChannelId(authenticatedUser, channelId)) {
                logger.error("Error, user {} is not subscribed to channel {}", authenticatedUser.getUsername(),
                             channelId
                );
                throw new UnauthorizedException(
                    String.format("Error, user %s is not subscribed to channel %s", authenticatedUser, channelId));
            }
            if (!channelService.exists(channelId)) {
                logger.error("Error, channel {} not present in the db", channelId);
                throw new ResourceNotFoundException(channelId);
            }
        });
        return videoRepository.findByChannelIdIn(channelIds, paging);
    }
}
