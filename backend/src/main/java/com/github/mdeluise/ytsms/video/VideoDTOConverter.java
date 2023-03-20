package com.github.mdeluise.ytsms.video;

import com.github.mdeluise.ytsms.common.AbstractDTOConverter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VideoDTOConverter extends AbstractDTOConverter<Video, VideoDTO> {
    @Autowired
    public VideoDTOConverter(ModelMapper modelMapper) {
        super(modelMapper);
    }


    @Override
    public Video convertFromDTO(com.github.mdeluise.ytsms.video.VideoDTO dto) {
        return modelMapper.map(dto, Video.class);
    }


    @Override
    public VideoDTO convertToDTO(com.github.mdeluise.ytsms.video.Video data) {
        return modelMapper.map(data, VideoDTO.class);
    }
}