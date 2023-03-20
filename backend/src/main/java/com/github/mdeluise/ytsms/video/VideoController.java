package com.github.mdeluise.ytsms.video;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/video")
@Tag(name = "Video", description = "Endpoints for operations on video.")
public class VideoController {
    private final VideoService videoService;
    private final VideoDTOConverter videoDtoConverter;


    @Autowired
    public VideoController(VideoService videoService, VideoDTOConverter videoDtoConverter) {
        this.videoService = videoService;
        this.videoDtoConverter = videoDtoConverter;
    }


    @GetMapping
    @Operation(summary = "Get all the Videos", description = "Get all the Videos.")
    public ResponseEntity<Page<VideoDTO>> findAll(@RequestParam(defaultValue = "0", required = false) Integer pageNo,
                                                  @RequestParam(defaultValue = "10", required = false) Integer pageSize,
                                                  @RequestParam(defaultValue = "publishedAt", required = false)
                                                  String sortBy, @RequestParam(defaultValue = "DESC", required = false)
                                                  Sort.Direction sortDir) {
        Pageable paging = PageRequest.of(pageNo, pageSize, sortDir, sortBy);
        Page<VideoDTO> result = videoService.getAll(paging).map(videoDtoConverter::convertToDTO);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/{channelIds}")
    @Operation(
        summary = "Get all the Videos published by the provided channels",
        description = "Get all the Videos published by the provided channels."
    )
    public ResponseEntity<Page<VideoDTO>> findAllByChannel(
        @RequestParam(defaultValue = "0", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize,
        @RequestParam(defaultValue = "publishedAt", required = false) String sortBy,
        @RequestParam(defaultValue = "DESC", required = false) Sort.Direction sortDir,
        @PathVariable List<String> channelIds) {
        Pageable paging = PageRequest.of(pageNo, pageSize, sortDir, sortBy);
        Page<VideoDTO> result = videoService.getAllByChannelIds(paging, channelIds).map(videoDtoConverter::convertToDTO);
        return ResponseEntity.ok(result);
    }
}
