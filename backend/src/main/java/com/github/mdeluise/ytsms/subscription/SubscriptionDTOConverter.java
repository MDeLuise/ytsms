package com.github.mdeluise.ytsms.subscription;

import com.github.mdeluise.ytsms.common.AbstractDTOConverter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionDTOConverter extends AbstractDTOConverter<Subscription, SubscriptionDTO> {
    private final ModelMapper modelMapper;


    @Autowired
    public SubscriptionDTOConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }


    @Override
    public Subscription convertFromDTO(SubscriptionDTO dto) {
        return modelMapper.map(dto, Subscription.class);
    }


    @Override
    public SubscriptionDTO convertToDTO(Subscription data) {
        return modelMapper.map(data, SubscriptionDTO.class);
    }
}