package com.github.mdeluise.ytsms.video.service;

import com.github.mdeluise.ytsms.TestEnvironment;
import com.github.mdeluise.ytsms.authentication.User;
import com.github.mdeluise.ytsms.authentication.UserService;
import com.github.mdeluise.ytsms.video.Video;
import com.github.mdeluise.ytsms.video.VideoRepository;
import com.github.mdeluise.ytsms.video.VideoService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ExtendWith(SpringExtension.class)
@WithMockUser(username = "user")
@Import(TestEnvironment.class)
public class VideoServiceTest {
    @Mock
    VideoRepository videoRepository;
    @Mock
    UserService userService;
    @InjectMocks
    VideoService videoService;


    @Test
    void whenSaveVideo_thenReturnVideo() {
        Video toSave = new Video();
        toSave.setId("0");
        toSave.setTitle("video title 0");
        Mockito.when(videoRepository.save(toSave)).thenReturn(toSave);
        Mockito.when(userService.get("user")).thenReturn(new User());

        Assertions.assertThat(videoService.save(toSave)).isSameAs(toSave);
    }

    @Test
    void whenGetAllVideos_thenReturnAllVideos() {
        Video toGet1 = new Video();
        toGet1.setId("0");
        toGet1.setTitle("video title 0");
        Video toGet2 = new Video();
        toGet2.setId("1");
        toGet2.setTitle("video title 1");

        List<Video> allVideos = List.of(toGet1, toGet2);
        Mockito.when(videoRepository.findByChannelIdIn(Mockito.any(), Mockito.any()))
               .thenReturn(new PageImpl<>(allVideos));
        Mockito.when(userService.get("user")).thenReturn(new User());

        Pageable paging = PageRequest.of(0, 10);
        Assertions.assertThat(videoService.getAll(paging).getContent().containsAll(allVideos));
        Assertions.assertThat(allVideos.containsAll(videoService.getAll(paging).getContent()));
    }
}
