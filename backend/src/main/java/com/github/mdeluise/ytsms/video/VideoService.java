package com.github.mdeluise.ytsms.video;

import com.github.mdeluise.ytsms.authentication.User;
import com.github.mdeluise.ytsms.authentication.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VideoService {
    private final VideoRepository videoRepository;
    private final UserService userService;


    @Autowired
    public VideoService(VideoRepository videoRepository, UserService userService) {
        this.videoRepository = videoRepository;
        this.userService = userService;
    }


    public Page<Video> getAll(Pageable pageable) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        String username = authentication.getName();
        User authenticatedUser = userService.get(username);
        Set<String> channelIdSubscribed =
            authenticatedUser.getSubscriptions().stream()
                             .map(sub -> sub.getChannel().getId())
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
        return videoRepository.findByChannelIdIn(channelIds, paging);
    }
}
