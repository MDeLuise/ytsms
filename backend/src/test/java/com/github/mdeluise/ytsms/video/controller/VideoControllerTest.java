package com.github.mdeluise.ytsms.video.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mdeluise.ytsms.scraper.VideoScraper;
import com.github.mdeluise.ytsms.scraper.VideoScraperFactory;
import com.github.mdeluise.ytsms.security.apikey.ApiKeyFilter;
import com.github.mdeluise.ytsms.security.apikey.ApiKeyRepository;
import com.github.mdeluise.ytsms.security.apikey.ApiKeyService;
import com.github.mdeluise.ytsms.security.jwt.JwtTokenFilter;
import com.github.mdeluise.ytsms.security.jwt.JwtTokenUtil;
import com.github.mdeluise.ytsms.security.jwt.JwtWebUtil;
import com.github.mdeluise.ytsms.video.Video;
import com.github.mdeluise.ytsms.video.VideoController;
import com.github.mdeluise.ytsms.video.VideoDTO;
import com.github.mdeluise.ytsms.video.VideoDTOConverter;
import com.github.mdeluise.ytsms.video.VideoService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

@WebMvcTest(VideoController.class)
@AutoConfigureMockMvc(addFilters = false)
public class VideoControllerTest {
    @MockBean
    JwtTokenFilter jwtTokenFilter;
    @MockBean
    JwtTokenUtil jwtTokenUtil;
    @MockBean
    JwtWebUtil jwtWebUtil;
    @MockBean
    ApiKeyFilter apiKeyFilter;
    @MockBean
    ApiKeyService apiKeyService;
    @MockBean
    ApiKeyRepository apiKeyRepository;
    @MockBean
    VideoService videoService;
    @MockBean
    VideoDTOConverter videoDTOConverter;
    @MockBean
    VideoScraperFactory videoScraperFactory;
    @MockBean
    VideoScraper videoScraper;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;


    @Test
    void whenGetVideos_ShouldReturnVideos() throws Exception {
        Video video1 = new Video();
        video1.setId("1");
        video1.setTitle("video title 1");
        VideoDTO videoDTO1 = new VideoDTO();
        videoDTO1.setId("1");
        videoDTO1.setTitle("video title 1");
        Video video2 = new Video();
        video2.setId("2");
        video2.setTitle("video title 2");
        VideoDTO videoDTO2 = new VideoDTO();
        videoDTO2.setId("2");
        videoDTO2.setTitle("video title 2");
        Mockito.when(videoService.getAll(Mockito.any())).thenReturn(new PageImpl<>(List.of(video1, video2)));
        Mockito.when(videoDTOConverter.convertToDTO(video1)).thenReturn(videoDTO1);
        Mockito.when(videoDTOConverter.convertToDTO(video2)).thenReturn(videoDTO2);
        Mockito.when(videoScraperFactory.getVideoScraper()).thenReturn(videoScraper);

        mockMvc.perform(MockMvcRequestBuilders.get("/video")).andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
               .andExpect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(2)));
    }

}
